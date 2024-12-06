package com.dicoding.zenvest

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.zenvest.adapter.CommoditiesAdapter
import com.dicoding.zenvest.adapter.MarketAdapter
import com.dicoding.zenvest.data.Commodity
import com.dicoding.zenvest.data.MarketItem
import com.dicoding.zenvest.retrofit.BtcService
import com.dicoding.zenvest.retrofit.EthService
import com.dicoding.zenvest.retrofit.SolService
import com.dicoding.zenvest.retrofit.XrpService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var marketAdapter: MarketAdapter
    private lateinit var commoditiesAdapter: CommoditiesAdapter
    private lateinit var watchlistRecyclerView: RecyclerView
    private lateinit var commoditiesRecyclerView: RecyclerView

    private val watchlistItems = mutableListOf<MarketItem>()
    private val commoditiesList = mutableListOf<Commodity>()

    private lateinit var retrofit: Retrofit
    private lateinit var btcService: BtcService
    private lateinit var ethService: EthService
    private lateinit var solService: SolService
    private lateinit var xrpService: XrpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://zenvest-prices-apis-747657276300.asia-southeast2.run.app/") // Use your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        btcService = retrofit.create(BtcService::class.java)
        ethService = retrofit.create(EthService::class.java)
        solService = retrofit.create(SolService::class.java)
        xrpService = retrofit.create(XrpService::class.java)

        // Initialize RecyclerViews
        watchlistRecyclerView = findViewById(R.id.watchlistRecyclerView)
        commoditiesRecyclerView = findViewById(R.id.commoditiesRecyclerView)

        // Set up RecyclerViews and adapters
        watchlistRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        marketAdapter = MarketAdapter(watchlistItems) { marketItem ->
            onMarketItemClick(marketItem)
        }


        watchlistRecyclerView.adapter = marketAdapter

        commoditiesRecyclerView.layoutManager = LinearLayoutManager(this)
        commoditiesAdapter = CommoditiesAdapter(commoditiesList)
        commoditiesRecyclerView.adapter = commoditiesAdapter

        // Fetch data from the API
        fetchCoinData()

        // Handle Search Input
        val searchInput = findViewById<EditText>(R.id.search_input)
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString()
                performSearch(query) // Custom function to handle search
                true
            } else {
                false
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment to HomeFragment
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
//                R.id.nav_explore -> {
//                    // Explore selected
//                    true
//                }
                R.id.nav_news -> {
                    val intent = Intent(this, NewsActivity::class.java) // Assuming you have a NewsActivity
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun onMarketItemClick(marketItem: MarketItem) {
        // Create an Intent to launch the appropriate activity based on the coin clicked
        val intent = when (marketItem.name) {
            "Bitcoin" -> Intent(this, BtcActivity::class.java)  // If the item is Bitcoin, open BtcActivity
            "Ethereum" -> Intent(this, EthActivity::class.java)  // If the item is Ethereum, open EthActivity
            "Solana" -> Intent(this, SolActivity::class.java) // If the item is Solana, open SolActivity
            "Ripple" -> Intent(this, XrpActivity::class.java) // If the item is Ripple, open SolActivity
            // Add more cases for other coin types as needed
            else -> null
        }

        // Check if the intent is valid (i.e., the coin type is recognized)
        if (intent != null) {
            // Pass the dynamic data to the corresponding activity
            intent.putExtra("coin_name", marketItem.name)
            intent.putExtra("current_price", marketItem.value)
            intent.putExtra("percentage_change", marketItem.percentage)

            // Start the appropriate activity
            startActivity(intent)
        } else {
            // Show a toast or log an error if the coin type is not supported
            Toast.makeText(this, "Coin type not supported: ${marketItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchCoinData() {
        lifecycleScope.launch {
            try {
                // Make the API call to fetch coin data
                val btcResponse = btcService.getCoinData()
                val ethResponse = ethService.getCoinData()
                val solResponse = solService.getCoinData()
                val xrpResponse = xrpService.getCoinData()

                if (btcResponse.isSuccessful && ethResponse.isSuccessful && solResponse.isSuccessful && xrpResponse.isSuccessful) {
                    val btcData = btcResponse.body()
                    val ethData = ethResponse.body()
                    val solData = solResponse.body()
                    val xrpData = xrpResponse.body()

                    // Log the raw data for debugging
                    Log.d("MainActivity", "Fetched BTC data: $btcData")
                    Log.d("MainActivity", "Fetched ETH data: $ethData")
                    Log.d("MainActivity", "Fetched SOL data: $solData")
                    Log.d("MainActivity", "Fetched XRP data: $xrpData")

                    if (btcData != null && ethData != null && solData != null && xrpData != null) {
                        // Safely add data to watchlistItems and commoditiesList
                        watchlistItems.clear()
                        commoditiesList.clear()

                        // Add data to watchlistItems
                        watchlistItems.add(MarketItem(
                            btcData.coin?.trim() ?: "Unknown BTC",
                            "$${btcData.current_price?.trim() ?: "0"}",
                            "+${btcData.percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        watchlistItems.add(MarketItem(
                            ethData.coin?.trim() ?: "Unknown ETH",
                            "$${ethData.current_price?.trim() ?: "0"}",
                            "+${ethData.percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        watchlistItems.add(MarketItem(
                            solData.coin?.trim() ?: "Unknown SOL",
                            "$${solData.current_price?.trim() ?: "0"}",
                            "+${solData.percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        watchlistItems.add(MarketItem(
                            xrpData.coin?.trim() ?: "Unknown XRP",
                            "$${xrpData.current_price?.trim() ?: "0"}",
                            "+${xrpData.percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        // Add data to commoditiesList (price and yearly_percentage)

                        commoditiesList.add(Commodity(
                            "XRP",
                            "+${xrpData.yearly_percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        commoditiesList.add(Commodity(
                            "BTC",
                            "+${btcData.yearly_percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        commoditiesList.add(Commodity(
                            "SOL",
                            "+${solData.yearly_percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        commoditiesList.add(Commodity(
                            "ETH",
                            "+${ethData.yearly_percentage?.toString()?.trim() ?: "0%"} %"
                        ))

                        // Notify the adapter to refresh the data in the RecyclerView
                        marketAdapter.notifyDataSetChanged()
                        commoditiesAdapter.notifyDataSetChanged()

                    } else {
                        Log.e("MainActivity", "One or more coin data are null!")
                    }
                } else {
                    if (!btcResponse.isSuccessful) {
                        Log.e("MainActivity", "Error fetching BTC data: ${btcResponse.errorBody()}")
                    }
                    if (!ethResponse.isSuccessful) {
                        Log.e("MainActivity", "Error fetching ETH data: ${ethResponse.errorBody()}")
                    }
                    if (!solResponse.isSuccessful) {
                        Log.e("MainActivity", "Error fetching SOL data: ${solResponse.errorBody()}")
                    }
                    if (!xrpResponse.isSuccessful) {
                        Log.e("MainActivity", "Error fetching XRP data: ${xrpResponse.errorBody()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching coin data: ${e.localizedMessage}")
            }
        }
    }

    // Function to handle the search and update RecyclerViews
    private fun performSearch(query: String) {
        // Filter Market List based on search query
        val filteredMarketItems = watchlistItems.filter {
            it.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // Filter Commodities List based on search query
        val filteredCommodities = commoditiesList.filter {
            it.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // Update RecyclerViews with filtered data
        marketAdapter.updateData(filteredMarketItems)
        commoditiesAdapter.updateData(filteredCommodities)

        // Show a Toast indicating the search term
        Toast.makeText(this, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }
}
