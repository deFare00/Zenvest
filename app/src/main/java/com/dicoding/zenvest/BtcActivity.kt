package com.dicoding.zenvest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.zenvest.data.CoinData
import com.dicoding.zenvest.data.MarketItem
import com.dicoding.zenvest.data.PredictionChart
import com.dicoding.zenvest.retrofit.BtcService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BtcActivity : AppCompatActivity() {

    private lateinit var marketCapValueTextView: TextView
    private lateinit var volumeValueTextView: TextView
    private lateinit var coinNameTextView: TextView
    private lateinit var currentPriceTextView: TextView
    private lateinit var percentageChangeTextView: TextView
    private lateinit var yearlyPercentageChangeTextView: TextView
    private lateinit var lineChart: LineChart

    private lateinit var retrofit: Retrofit
    private lateinit var btcService: BtcService
//    private lateinit var ethService: EthService
//    private lateinit var solService: SolService
//    private lateinit var xrpService: XrpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btc)

        // Show a simple toast to confirm that BtcActivity is launched
        Toast.makeText(this, "BtcActivity Launched", Toast.LENGTH_SHORT).show()

        val coinName = intent.getStringExtra("coin_name")
        val currentPrice = intent.getStringExtra("current_price")
        val percentageChange = intent.getStringExtra("percentage_change")
        val volumeText = intent.getStringExtra("volume")
        val marketcapText = intent.getStringExtra("marketcap")

        // Log the received data to verify it's correct
        Log.d("BtcActivity", "Received data: coin_name=$coinName, current_price=$currentPrice, percentage_change=$percentageChange")

        // Initialize views
        marketCapValueTextView = findViewById(R.id.marketCapValueTextView)
        volumeValueTextView = findViewById(R.id.volumeValueTextView)
        coinNameTextView = findViewById(R.id.coinNameTextView)
        currentPriceTextView = findViewById(R.id.currentPriceTextView)
        percentageChangeTextView = findViewById(R.id.percentageChangeTextView)
        yearlyPercentageChangeTextView = findViewById(R.id.yearlyPercentageChangeTextView)
        lineChart = findViewById(R.id.lineChart)

        // Set the received data to the UI
        marketCapValueTextView.text = marketcapText
        volumeValueTextView.text = volumeText
        coinNameTextView.text = coinName
        currentPriceTextView.text = currentPrice
        percentageChangeTextView.text = percentageChange

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://zenvest-prices-apis-747657276300.asia-southeast2.run.app/") // Use your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        btcService = retrofit.create(BtcService::class.java)
//        ethService = retrofit.create(EthService::class.java)
//        solService = retrofit.create(SolService::class.java)
//        xrpService = retrofit.create(XrpService::class.java)

        // Fetch coin data from the API
        fetchCoinData()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment to HomeFragment
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
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

    private fun fetchCoinData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch data only for BTC
                val btcResponse = btcService.getCoinData()

                if (btcResponse.isSuccessful) {
                    // Parse the data for BTC
                    val btcData = btcResponse.body()

                    // Switch to the main thread to update the UI
                    withContext(Dispatchers.Main) {
                        // Update UI for BTC
                        updateCoinUI(btcData, "BTC")
                    }
                } else {
                    Log.e("BtcActivity", "Error fetching data for BTC: ${btcResponse.errorBody()}")
                }
            } catch (e: Exception) {
                Log.e("BtcActivity", "Exception while fetching data: ${e.localizedMessage}")
            }
        }
    }

    private fun updateCoinUI(coinData: CoinData?, coinType: String) {
        if (coinData != null) {
            // Update UI for the coin (e.g., name, price, percentage change)
            coinNameTextView.text = coinData.coin ?: "Unknown Coin"
            currentPriceTextView.text = "$${coinData.current_price ?: "0"}"
            percentageChangeTextView.text = "Change +${coinData.percentage ?: "0%"}%"
            yearlyPercentageChangeTextView.text = "Yearly +${coinData.yearly_percentage ?: "0"}%"
            volumeValueTextView.text = coinData.volume ?: "0"
            marketCapValueTextView.text = coinData.marketcap ?: "0"

            // Set up the line chart for each coin using prediction chart data
            setupLineChart(coinData, coinType)
        }
    }

    private fun setupLineChart(coinData: CoinData?, coinType: String) {
        val actualEntries = mutableListOf<Entry>()
        val futureEntries = mutableListOf<Entry>()
        val historyEntries = mutableListOf<Entry>()

        val predictionChart = coinData?.prediction_chart

        // Helper function to parse time
        fun parseTime(time: String?): Float {
            time?.let {
                try {
                    // Adjust the time format based on your actual API response
                    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.getDefault()) // Adjust format if necessary
                    val date = dateFormat.parse(it)
                    return date?.time?.toFloat() ?: 0f
                } catch (e: Exception) {
                    Log.e("BtcActivity", "Error parsing time: ${e.message}")
                    return 0f // Default to 0 if parsing fails
                }
            }
            return 0f // Return 0 if the time is null
        }

        // Add actual price data points with time
        predictionChart?.actual_price?.forEach { priceData ->
            val price = priceData.value.replace(",", "").toFloatOrNull() ?: 0f
            val time = priceData.time

            if (time.isNullOrEmpty()) {
                Log.e("BtcActivity", "Time is null or empty for actual price: $priceData")
            }

            val timeInMillis = parseTime(time)

            actualEntries.add(Entry(timeInMillis, price))
        }

        // Add future prediction data points with time
        predictionChart?.future_prediction?.forEach { priceData ->
            val price = priceData.value.replace(",", "").toFloatOrNull() ?: 0f
            val time = priceData.time

            if (time.isNullOrEmpty()) {
                Log.e("BtcActivity", "Time is null or empty for future prediction: $priceData")
            }

            val timeInMillis = parseTime(time)

            futureEntries.add(Entry(timeInMillis, price))
        }

        // Add history predicted data points with time
        predictionChart?.history_predicted?.forEach { priceData ->
            val price = priceData.value.replace(",", "").toFloatOrNull() ?: 0f
            val time = priceData.time

            if (time.isNullOrEmpty()) {
                Log.e("BtcActivity", "Time is null or empty for history predicted: $priceData")
            }

            val timeInMillis = parseTime(time)

            historyEntries.add(Entry(timeInMillis, price))
        }

        // Create datasets for actual, future, and history predicted data
        val actualDataSet = LineDataSet(actualEntries, "$coinType Actual Price")
        actualDataSet.color = Color.GREEN
        actualDataSet.lineWidth = 2f
        actualDataSet.setDrawCircles(false)

        val futureDataSet = LineDataSet(futureEntries, "$coinType Future Prediction")
        futureDataSet.color = Color.RED
        futureDataSet.lineWidth = 2f
        futureDataSet.setDrawCircles(false)

        val historyDataSet = LineDataSet(historyEntries, "$coinType History Predicted")
        historyDataSet.color = Color.BLUE
        historyDataSet.lineWidth = 2f
        historyDataSet.setDrawCircles(false)

        // Add all datasets to the chart
        val lineData = LineData(actualDataSet, futureDataSet, historyDataSet)

        // Set the data to the chart and refresh it
        lineChart.data = lineData

        // Set up the X-Axis to show the month and year
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val timeInMillis = value.toLong()

                return try {
                    val date = Date(timeInMillis)
                    val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    monthYearFormat.format(date)
                } catch (e: Exception) {
                    "N/A"
                }
            }
        }

        // Set up the Y-Axis to show price values as currency
        val yAxis = lineChart.axisLeft
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "$%.2f".format(value)
            }
        }
        lineChart.axisLeft.axisMinimum = 2000f
        lineChart.axisLeft.axisMaximum = 100000f

        // Customize other chart elements (e.g., axis labels)
        lineChart.invalidate() // Refresh the chart
    }
}