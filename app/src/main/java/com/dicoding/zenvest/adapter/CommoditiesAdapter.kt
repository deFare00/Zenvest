package com.dicoding.zenvest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.R
import com.dicoding.zenvest.data.Commodity

class CommoditiesAdapter(private var commodities: MutableList<Commodity>) : RecyclerView.Adapter<CommoditiesAdapter.CommodityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommodityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commodity, parent, false)
        return CommodityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommodityViewHolder, position: Int) {
        val commodity = commodities[position]
        holder.commodityName.text = commodity.name
        holder.commodityPercentage.text = commodity.yearly_percentage
    }

    override fun getItemCount(): Int = commodities.size

    // Add the updateData method
    fun updateData(newData: List<Commodity>) {
        commodities.clear()  // Clear the current list
        commodities.addAll(newData)  // Add the new data
        notifyDataSetChanged()  // Notify RecyclerView to refresh
    }

    class CommodityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commodityName: TextView = itemView.findViewById(R.id.commodity_name)
        val commodityPercentage: TextView = itemView.findViewById(R.id.commodityYearlyPercentage)
    }
}