/*
 *  _____ _                       _             _     _     _
 * /  ___| |                     (_)           | |   (_)   | |
 * \ `--.| |__   ___  _ __  _ __  _ _ __   __ _| |    _ ___| |_
 *  `--. \ '_ \ / _ \| '_ \| '_ \| | '_ \ / _` | |   | / __| __|
 * /\__/ / | | | (_) | |_) | |_) | | | | | (_| | |___| \__ \ |_
 * \____/|_| |_|\___/| .__/| .__/|_|_| |_|\__, \_____/_|___/\__|
 *                   | |   | |             __/ |
 *                   |_|   |_|            |___/
 *
 * Copyright (C) 2021-2022 Sebastian Göls
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License only
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package pl.edu.pjwstk.s999844.shoppinglist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_item.*
import pl.edu.pjwstk.s999844.shoppinglist.dal.ShoppingListDao
import pl.edu.pjwstk.s999844.shoppinglist.dal.ShoppingListDatabase
import pl.edu.pjwstk.s999844.shoppinglist.models.RequiredItem
import java.util.*

class AddItemActivity : AbstractShoppingActivity() {
	companion object {
		const val NameParameterName = "name"
		const val AmountParameterName = "amount"
	}

	private val inputMethodManager: InputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	private val currentLocale: Locale
		get() = baseContext.resources.configuration.locales.get(0)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_item)
	}


	override fun onStart() {
		super.onStart()

		// german-speaking people capitalize nouns - as a shopping list usually contains nouns we capitalize the first letter automatically. They can still use lowercase manually if they need a pronoun
		when (currentLocale.language) {
			Locale.GERMAN.language -> addItemNameInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or addItemNameInput.inputType
		}
		addItemNameInput.requestFocus()

		title = getString(R.string.addTitleBarText)

		val data: Uri = intent?.data
			?: return

		val nameParameter = data.getQueryParameter(NameParameterName)
			?: return
		if (nameParameter.isEmpty() || nameParameter.isBlank()) {
			return
		}

		addItemNameInput.setText(nameParameter, TextView.BufferType.EDITABLE)

		val amountParameter = data.getQueryParameter(AmountParameterName) ?: return
		if (amountParameter.isEmpty() || amountParameter.isBlank()) {
			return
		}

		val amount: Int = try {
			amountParameter.toInt()
		} catch (e: java.lang.NumberFormatException) {
			return
		}

		if (amount > 1) {
			addItemAmountInput.setText(amount.toString(), TextView.BufferType.EDITABLE)
		}
	}

	private fun createShareUri(item: RequiredItem): String {
		var uriBuilder = Uri.Builder().scheme(getString(R.string.shareScheme)).authority(getString(R.string.shareHost)).path(getString(R.string.sharePath)).appendQueryParameter(NameParameterName, item.name)
		if (item.amount > 1) {
			uriBuilder = uriBuilder.appendQueryParameter(AmountParameterName, item.amount.toString())
		}
		return uriBuilder.toString()
	}

	private fun createAndValidateItem(): RequiredItem? {
		val name: String = addItemNameInput.text.toString().trim()
		if (name.isEmpty()) {
			warnError(getString(R.string.addNameIsEmptyMessage))
			return null
		}

		val amountString = addItemAmountInput.text.toString().trim()
		val amount: Int = if (amountString.isEmpty() || amountString.isBlank()) {
			1
		} else {
			try {
				amountString.toInt()
			} catch (e: NumberFormatException) {
				warnError(getString(R.string.addAmountNotANumberMessage))
				return null
			}
		}

		if (amount <= 0) {
			warnError(getString(R.string.addAmountTooSmallMessage))
			return null
		}

		return RequiredItem(name, amount)
	}

	@Suppress("UNUSED_PARAMETER")
	fun share(view: View) {
		val item = createAndValidateItem() ?: return

		val sendIntent: Intent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, getString(R.string.addShareText, createShareUri(item)))
			type = "text/plain"
		}

		val shareIntent = Intent.createChooser(sendIntent, null)
		startActivity(shareIntent)
	}

	@Suppress("UNUSED_PARAMETER")
	fun addItem(view: View) {
		inputMethodManager.hideSoftInputFromWindow(addItemNameInput.windowToken, 0)

		val item = createAndValidateItem() ?: return

		val shoppingListDao: ShoppingListDao = ShoppingListDatabase.getInstance(applicationContext).getShoppingListDao()

		if (shoppingListDao.findByNameLike(item.name).isNotEmpty()) {
			warnError(getString(R.string.addItemAlreadyExistsMessage))
			return
		}

		shoppingListDao.add(item)

		finish()
	}

	private fun warnError(message: String) = Snackbar.make(saveButton, message, Snackbar.LENGTH_SHORT).show()
}
