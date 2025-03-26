package com.streakfreak.focusflow


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class ItemAdapter(context: Context, private val items: List<ListItem>) :
    ArrayAdapter<ListItem>(context, R.layout.list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item, parent, false)

        val item = items[position]

        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val subtitleView: TextView = view.findViewById(R.id.itemSubtitle)
        val containerView: View = view.findViewById(R.id.itemContainer)

        // Set image
        imageView.setImageResource(item.image)

        // Set text
        titleView.text = item.title
        subtitleView.text = item.subtitle
        Toast.makeText(context, item.toString(), Toast.LENGTH_SHORT).show()

        // Set background color
        containerView.setBackgroundResource(item.color)

        return view
    }
}
