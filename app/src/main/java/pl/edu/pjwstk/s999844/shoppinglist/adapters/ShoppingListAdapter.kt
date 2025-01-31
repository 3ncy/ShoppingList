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

package pl.edu.pjwstk.s999844.shoppinglist.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.shopping_list_item.view.*
import pl.edu.pjwstk.s999844.shoppinglist.databinding.ShoppingListItemBinding
import pl.edu.pjwstk.s999844.shoppinglist.models.RequiredItem
import java.util.function.BiConsumer

class ShoppingListAdapter(private val changeAmountCallback: BiConsumer<RequiredItem, Int>) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {
	private var items: List<RequiredItem> = listOf()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val inflater: LayoutInflater = LayoutInflater.from(parent.context)
		val binding: ShoppingListItemBinding = ShoppingListItemBinding.inflate(inflater)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item: RequiredItem = items[position]

		val binding = holder.shoppingListItem
		binding.amountTextView.text = item.amount.toString()
		binding.amountTextView.isVisible = item.amount > 1

		binding.nameTextView.text = item.name

		binding.buttonLayout.addButton.setOnClickListener {
			changeAmountCallback.accept(item, 1)
		}
		binding.buttonLayout.subtractButton.setOnClickListener {
			changeAmountCallback.accept(item, -1)
		}
		binding.buttonLayout.deleteButton.setOnClickListener {
			changeAmountCallback.accept(item, -item.amount)
		}
	}

	override fun getItemCount(): Int = items.size

	@SuppressLint("NotifyDataSetChanged")
	fun setItems(items: List<RequiredItem>) {
		this.items = items
		notifyDataSetChanged()
	}

	class ViewHolder(val shoppingListItem: ShoppingListItemBinding) : RecyclerView.ViewHolder(shoppingListItem.root)
}
