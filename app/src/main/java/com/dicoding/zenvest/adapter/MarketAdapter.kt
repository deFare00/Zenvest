package com.dicoding.zenvest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.R
import com.dicoding.zenvest.data.MarketItem
import java.text.DecimalFormat

class MarketAdapter(private var watchlist: List<MarketItem>, private val itemClickListener: (MarketItem) -> Unit) : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watchlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = watchlist[position]

        // Set stock name
        holder.stockName.text = item.name

        // Format the price to two decimal places
        holder.stockValue.text = item.value.toString()

        // Set percentage change
        holder.stockChange.text = item.percentage

        // Set click listener for each item
        holder.itemView.setOnClickListener {
            itemClickListener(item)
        }
    }

    override fun getItemCount(): Int = watchlist.size

    fun updateData(newData: List<MarketItem>) {
        watchlist = newData
        notifyDataSetChanged() // Notify adapter to refresh the data
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockName: TextView = itemView.findViewById(R.id.stock_name)
        val stockValue: TextView = itemView.findViewById(R.id.stock_value)
        val stockChange: TextView = itemView.findViewById(R.id.stock_change)
    }
}