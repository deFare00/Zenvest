package com.dicoding.zenvest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.R
import com.dicoding.zenvest.data.MarketItem

class MarketAdapter(private val watchlist: List<MarketItem>) : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watchlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = watchlist[position]
        holder.stockName.text = item.name
        holder.stockValue.text = item.value.toString()
        holder.stockChange.text = item.change
    }

    override fun getItemCount(): Int = watchlist.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val stockName: TextView = itemView.findViewById(R.id.stock_name)
        val stockValue: TextView = itemView.findViewById(R.id.stock_value)
        val stockChange: TextView = itemView.findViewById(R.id.stock_change)
    }
}