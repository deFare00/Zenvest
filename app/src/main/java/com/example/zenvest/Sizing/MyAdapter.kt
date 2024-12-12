package com.example.zenvest.Sizing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationbarkotlin.R

class MyAdapter(
        private val context: Context,
        private val mData: List<String>
) : RecyclerView.Adapter<MyViewHolder>() {

private val item: MutableList<String> = ArrayList()
private var adapterItems: ArrayAdapter<String>? = null
private val selectedItems: HashMap<Int, String> = HashMap()

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    return MyViewHolder(view)
}

override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    holder.txtName.text = mData[position]
    adapterItems = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, item)
    holder.autoCompleteTextView.setAdapter(adapterItems)

    holder.autoCompleteTextView.setOnItemClickListener { _, _, i, _ ->
            val selectedOption = adapterItems?.getItem(i)
        if (selectedOption != null) {
            selectedItems[position] = selectedOption
        }
    }
}

override fun getItemCount(): Int {
    return mData.size
}

fun getSelectedItems(): HashMap<Int, String> {
    return selectedItems
}

fun updateItems(newItems: List<String>) {
    item.clear()
    item.addAll(newItems) // Update the item list
    notifyDataSetChanged() // Notify the adapter about the data change
}
}
