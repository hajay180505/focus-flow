package com.streakfreak.focusflow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class CustomSpinnerAdapter<T>(
    context: Context,
    items: List<T>,
    private val dropdownLayoutResource: Int = R.layout.spinner_item, // default
    private val selectedItemLayoutResource: Int = R.layout.spinner_selected_item // default
) : ArrayAdapter<T>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, selectedItemLayoutResource)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent, dropdownLayoutResource)
    }

    private fun getCustomView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        layoutResource: Int
    ): View {
        val view =
            convertView ?: LayoutInflater.from(context).inflate(layoutResource, parent, false)
        val item = getItem(position)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = item?.toString() // Or access specific properties of your data

        // You can programmatically change the text color here if needed based on the item:
        // if (item is MyItem && item.isSpecial) {
        //     textView.setTextColor(context.resources.getColor(R.color.special_item_color, null))
        // } else {
        //     textView.setTextColor(context.resources.getColor(R.color.spinner_text_color, null))
        // }

        return view
    }
}