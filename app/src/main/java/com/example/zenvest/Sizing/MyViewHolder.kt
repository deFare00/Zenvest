package com.example.zenvest.Sizing

import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationbarkotlin.R

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val txtName: TextView = itemView.findViewById(R.id.txt_name)
    val autoCompleteTextView: AutoCompleteTextView = itemView.findViewById(R.id.auto_complete_txt)
}
