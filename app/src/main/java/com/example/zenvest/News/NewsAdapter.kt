package com.example.zenvest.News

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.navigationbarkotlin.R
import java.text.SimpleDateFormat
import java.util.Locale

class NewsAdapter(private var newsItems: List<NewsItem>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.news_image)
        val newsTitle: TextView = itemView.findViewById(R.id.news_title)
        val newsDescription: TextView = itemView.findViewById(R.id.news_description)
        val newsPublished: TextView = itemView.findViewById(R.id.news_published)
        val newsLayout: LinearLayout = itemView.findViewById(R.id.news_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsItems[position]

        // Set the title and description
        holder.newsTitle.text = newsItem.title
        holder.newsDescription.text = newsItem.description

        // Format the published date to relative time span
        try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = sdf.parse(newsItem.published)
            val time = date?.time ?: 0L
            holder.newsPublished.text = DateUtils.getRelativeTimeSpanString(time)
        } catch (e: Exception) {
            e.printStackTrace()
            holder.newsPublished.text = "Unknown time"
        }

        // Load the image if imageUrl is available, otherwise use a placeholder
        if (!newsItem.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(newsItem.imageUrl) // Use the image URL from the data
                .placeholder(R.drawable.news) // Optional placeholder
                .into(holder.newsImage)
        } else {
            // If no image is available, set a placeholder image
            holder.newsImage.setImageResource(R.drawable.news)
        }

        // Set the link action to open the article in the browser
        holder.newsLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.link))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = newsItems.size

    // Method to update data in RecyclerView
    fun updateData(newNewsItems: List<NewsItem>) {
        this.newsItems = newNewsItems
        notifyDataSetChanged() // Notify RecyclerView that the data has changed
    }
}