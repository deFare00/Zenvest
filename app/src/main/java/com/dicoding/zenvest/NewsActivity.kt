package com.dicoding.zenvest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.adapter.NewsAdapter
import com.dicoding.zenvest.data.NewsItem
import com.dicoding.zenvest.retrofit.RetrofitInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NewsActivity : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        // Set the selected item in BottomNavigationView to News (assuming ID is nav_news)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_news
        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
//                R.id.nav_explore -> {
//                    true
//                }
                R.id.nav_news -> {
                    // Navigate to NewsActivity (News tab is already selected)
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Set the title of the page
        val title = findViewById<TextView>(R.id.news_title)
        title.text = getString(R.string.news_page)

        // Set up RecyclerView for displaying news articles
        newsRecyclerView = findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with an empty list
        newsAdapter = NewsAdapter(emptyList())
        newsRecyclerView.adapter = newsAdapter

        // Fetch news data from API
        fetchNewsData()

        // Handle the back button click (ImageButton)
//        val backButton = findViewById<ImageButton>(R.id.back_button)
//        backButton.setOnClickListener {
//            Log.d("NewsActivity", "Back button clicked")
//            navigateToMainActivity()
//        }

        // If API level is 31 or lower, use onBackPressed() to handle the back press
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            // Override onBackPressed() for older Android versions (pre-API 33)
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d("NewsActivity", "Back pressed (onBackPressed) - API 31 and below")
                    navigateToMainActivity()
                }
            })
        }
    }

    private fun fetchNewsData() {
        val topics = listOf("btc", "sol", "eth")  // Topics for news
        val allNewsItems = mutableListOf<NewsItem>()

        lifecycleScope.launch {
            try {
                // Fetch news for each topic and add them to the list
                for (topic in topics) {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.apiService.getNews(topic)
                    }

                    if (response.isSuccessful) {
                        response.body()?.let {
                            allNewsItems.addAll(it) // Add news to the list
                        }
                    } else {
                        Log.e("NewsActivity", "Failed to fetch news for topic $topic")
                    }
                }

                // Update the RecyclerView with the combined news data
                newsAdapter.updateData(allNewsItems)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@NewsActivity, "Error fetching news", Toast.LENGTH_SHORT).show()
                Log.e("NewsActivity", "Error fetching news: ${e.localizedMessage}")
            }
        }
    }

    // Helper function to fetch news for a specific topic
    private suspend fun fetchNewsForTopic(topic: String): List<NewsItem> {
        return try {
            // Retrofit API call to fetch news data for the given topic
            val response = RetrofitInstance.apiService.getNews(topic)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList<NewsItem>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("NewsActivity", "Error fetching news for $topic: ${e.localizedMessage}")
            emptyList<NewsItem>()
        }
    }

    // Helper function to navigate back to MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()  // Ensure that NewsActivity is removed from the back stack
    }

    // Override onBackPressed() for API <= 32 (deprecated for API >= 33)
    @Deprecated("Use OnBackPressedDispatcher for API >= 33", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
        Log.d("NewsActivity", "Back pressed (onBackPressed) - API <= 32")
        navigateToMainActivity()
    }
}