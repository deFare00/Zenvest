package com.example.zenvest.News

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationbarkotlin.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewsFragment : Fragment() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        // Set up RecyclerView for displaying news articles
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with an empty list
        newsAdapter = NewsAdapter(emptyList())
        newsRecyclerView.adapter = newsAdapter

        fetchNewsData()
        return view
    }

    private fun fetchNewsData() {
        val topics = listOf("btc", "sol", "eth")  // Topics for news
        val allNewsItems = mutableListOf<NewsItem>()

        lifecycleScope.launch() {
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
                        Log.e("FragmentNews", "Failed to fetch news for topic $topic")
                    }
                }

                // Update the RecyclerView with the combined news data
                newsAdapter.updateData(allNewsItems)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error fetching news", Toast.LENGTH_SHORT).show()
                Log.e("FragmentNews", "Error fetching news: ${e.localizedMessage}")
            }
        }
    }
}