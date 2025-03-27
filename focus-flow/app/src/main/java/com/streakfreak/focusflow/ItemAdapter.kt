package com.streakfreak.focusflow


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemAdapter(context: Context, private var items: List<ListItem>) :
    ArrayAdapter<ListItem>(context, R.layout.list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item, parent, false)

        val item = items[position]

        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val titleView: TextView = view.findViewById(R.id.itemTitle)
        val subtitleView: TextView = view.findViewById(R.id.itemSubtitle)
        val containerView: View = view.findViewById(R.id.itemContainer)
        val streakText = view.findViewById<TextView>(R.id.streakCountTextView)

        // Set image
        imageView.setImageResource(item.image)

        // Set text
        titleView.text = item.title
        subtitleView.text = item.subtitle
//        Toast.makeText(context, item.toString(), Toast.LENGTH_SHORT).show()
        streakText.text = item.streak

        containerView.setOnLongClickListener{
            showPopupMenu(view, position)
            true
        }

        // Set background color
        containerView.setBackgroundResource(item.color)

        return view
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu) // Assuming you have popup_menu.xml

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {  // Assuming "Delete" item has ID action_delete
                    deleteItem(position)
                    true // Consume the event
                }
                else -> false // Don't consume for other menu items (if any)
            }
        }
        popup.show()
    }
    private fun deleteItem(position: Int) {
        val itemToDelete = items[position]
        val db: Database = Database(context)
        // Assuming you have a coroutine scope available (e.g., from an Activity/Fragment)
        // If not, you'll need to manage your coroutine scope differently (see notes below).
        (context as? androidx.fragment.app.FragmentActivity)?.lifecycleScope?.launch {
            withContext(Dispatchers.IO) {
                try {
                    db.deleteByUserAndApp(itemToDelete.subtitle.lowercase() , itemToDelete.title.lowercase()) // Use your database helper's delete function
                    Log.d("ItemAdapter", "Deleted item: ${itemToDelete.title}")

                    // Update the list on the main thread
                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e("ItemAdapter", "Error deleting item: ${e.message}", e)
                    // Handle database deletion error appropriately (e.g., show a message)

                }
            }
        }
            ?: Log.e("ItemAdapter", "Context is not a FragmentActivity.  Cannot launch coroutine.")
        // Handle case when there is no fragment activity and hence no lifecyclescope

    }

}