package com.dicoding.zenvest

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.adapter.MarketAdapter
import com.dicoding.zenvest.data.MarketItem

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.search_input)

        // Search bar functionality
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString()
                performSearch(query) // Custom function to handle search
                true
            } else {
                false
            }
        }

        // Set up RecyclerView (horizontal scrollable list)
        val watchlistRecyclerView = findViewById<RecyclerView>(R.id.watchlistRecyclerView)

        // Example data for RecyclerView
        val watchlistItems = listOf(
            MarketItem("S&P 500", 4204.12, "+0.08%"),
            MarketItem("Dow 30", 34529.46, "+0.19%"),
            MarketItem("DAX", 15519.980, "+0.74%"),
            MarketItem("UK 100", 7022.6, "-0.53%"),
            MarketItem("Nikkei 225", 29149.19, "+2.10%")
        )

        // Set up adapter
        watchlistRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val marketAdapter = MarketAdapter(watchlistItems)
        watchlistRecyclerView.adapter = marketAdapter
    }

    // Custom search function (for demonstration purposes)
    private fun performSearch(query: String) {
        // Handle the search logic here
        Toast.makeText(this, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }
}
