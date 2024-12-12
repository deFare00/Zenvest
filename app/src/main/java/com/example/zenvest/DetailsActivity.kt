package com.example.navigationbarkotlin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.navigationbarkotlin.R
import com.example.zenvest.Fragments.CoinData
import com.example.zenvest.Fragments.PredictionChart
import com.example.zenvest.Fragments.PriceData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var coinNameTextView: TextView
    private lateinit var coinSymbolTextView: TextView
    private lateinit var currentPriceTextView: TextView
    private lateinit var percentageChangeTextView: TextView
    private lateinit var volumeValueTextView: TextView
    private lateinit var marketCapValueTextView: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Initialize Views
        lineChart = findViewById(R.id.line_chart)
        coinNameTextView = findViewById(R.id.coin_name)
        coinSymbolTextView = findViewById(R.id.coin_symbol)
        currentPriceTextView = findViewById(R.id.coin_price)
        percentageChangeTextView = findViewById(R.id.coin_percentage)
        volumeValueTextView = findViewById(R.id.coin_volume)
        marketCapValueTextView = findViewById(R.id.coin_market_cap)
        backButton = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            finish() // Closes the current activity and goes back to the previous one
        }

        // Get coin symbol from Intent
        val coinSymbol = intent.getStringExtra("symbol") ?: "BTC" // Default to BTC if no symbol
        fetchCoinData(coinSymbol)
    }

    private fun fetchCoinData(coinSymbol: String) {
        val url = "https://zenvest-prices-apis-747657276300.asia-southeast2.run.app/prices/$coinSymbol"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Parse primary coin information
                    val name = response.getString("coin")
                    val coinSymbol = response.getString("coin_symbol")
                    val currentPrice = response.getString("current_price")
                    val marketCap = response.getString("marketcap")
                    val percentage = response.getString("percentage")
                    val volume = response.optString("volume", "N/A")
                    val yearlyPercentage = response.optInt("yearly_percentage", 0)

                    // Parse Prediction Data
                    var actualPrices: List<PriceData>? = null
                    var futurePredictions: List<PriceData>? = null
                    var historyPredicted: List<PriceData>? = null

                    if (response.has("prediction_chart")) {
                        val predictionChart = response.getJSONObject("prediction_chart")

                        // Parse actual prices
                        if (predictionChart.has("actual_price")) {
                            val actualPriceArray = predictionChart.getJSONArray("actual_price")
                            actualPrices = List(actualPriceArray.length()) { index ->
                                val data = actualPriceArray.getJSONObject(index)
                                PriceData(data.getString("time"), data.getString("value"))
                            }
                        }

                        // Parse future predictions
                        if (predictionChart.has("future_prediction")) {
                            val futurePredictionArray = predictionChart.getJSONArray("future_prediction")
                            futurePredictions = List(futurePredictionArray.length()) { index ->
                                val data = futurePredictionArray.getJSONObject(index)
                                PriceData(data.getString("time"), data.getString("value"))
                            }
                        }

                        // Parse history predicted
                        if (predictionChart.has("history_predicted")) {
                            val historyPredictedArray = predictionChart.getJSONArray("history_predicted")
                            historyPredicted = List(historyPredictedArray.length()) { index ->
                                val data = historyPredictedArray.getJSONObject(index)
                                PriceData(data.getString("time"), data.getString("value"))
                            }
                        }
                    }

                    // Create `PredictionChart` and `CoinData`
                    val predictionChart = PredictionChart(
                        actual_price = actualPrices,
                        future_prediction = futurePredictions,
                        history_predicted = historyPredicted
                    )
                    val coinData = CoinData(
                        coin = name,
                        coin_symbol = coinSymbol,
                        current_price = currentPrice,
                        marketcap = marketCap,
                        percentage = percentage,
                        volume = volume,
                        yearly_percentage = yearlyPercentage,
                        prediction_chart = predictionChart
                    )

                    // Update UI and Chart
                    updateUI(coinData)
                    setupLineChart(coinData.prediction_chart)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to parse data.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network Error: Unable to fetch data.", Toast.LENGTH_SHORT).show()
            }
        )

        // Add Request to Volley Queue
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateUI(coinData: CoinData) {
        coinNameTextView.text = coinData.coin ?: "Unknown Coin"
        coinSymbolTextView.text = coinData.coin_symbol
        currentPriceTextView.text = "$ ${coinData.current_price}"

        val percentage = coinData.percentage?.toFloatOrNull() ?: 0f
        if(percentage >= 0) {
            percentageChangeTextView.text = "▲ ${Math.abs(percentage)}%"
            percentageChangeTextView.setTextColor(Color.GREEN)
        }
        else {
            percentageChangeTextView.text = "▼ ${Math.abs(percentage)}%"
            percentageChangeTextView.setTextColor(Color.RED)
        }
        volumeValueTextView.text = "$ ${coinData.volume}"
        marketCapValueTextView.text = "$ ${coinData.marketcap}"
    }

    private fun setupLineChart(predictionChart: PredictionChart) {
        val actualEntries = mutableListOf<Entry>()
        val futureEntries = mutableListOf<Entry>()
        val historyEntries = mutableListOf<Entry>()

        // Helper function to parse time
        fun parseTimeToMillis(time: String?): Float {
            time?.let {
                try {
                    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                    val date = dateFormat.parse(it)
                    return date?.time?.toFloat() ?: 0f
                } catch (e: Exception) {
                    Log.e("DetailsActivity", "Error parsing time: ${e.message}")
                    return 0f
                }
            }
            return 0f
        }

        // Extract and normalize data
        val allValues = mutableListOf<Float>()
        predictionChart.actual_price?.forEach { allValues.add(it.value.toFloatOrNull() ?: 0f) }
        predictionChart.future_prediction?.forEach { allValues.add(it.value.toFloatOrNull() ?: 0f) }
        predictionChart.history_predicted?.forEach { allValues.add(it.value.toFloatOrNull() ?: 0f) }

        // Find the max value for normalization
        val maxValue = allValues.maxOrNull() ?: 1f

        // Add data points and normalize values
        predictionChart.actual_price?.forEach {
            val normalizedValue = (it.value.toFloatOrNull() ?: 0f) / maxValue
            actualEntries.add(Entry(parseTimeToMillis(it.time), normalizedValue))
        }
        predictionChart.future_prediction?.forEach {
            val normalizedValue = (it.value.toFloatOrNull() ?: 0f) / maxValue
            futureEntries.add(Entry(parseTimeToMillis(it.time), normalizedValue))
        }
        predictionChart.history_predicted?.forEach {
            val normalizedValue = (it.value.toFloatOrNull() ?: 0f) / maxValue
            historyEntries.add(Entry(parseTimeToMillis(it.time), normalizedValue))
        }

        // Create datasets
        val actualDataSet = LineDataSet(actualEntries, "Actual Prices").apply {
            color = Color.GREEN
            lineWidth = 2f
            setDrawCircles(false)
        }
        val futureDataSet = LineDataSet(futureEntries, "Future Predictions").apply {
            color = Color.RED
            lineWidth = 2f
            setDrawCircles(false)
        }
        val historyDataSet = LineDataSet(historyEntries, "History Predicted").apply {
            color = Color.BLUE
            lineWidth = 2f
            setDrawCircles(false)
        }

        // Set data to chart
        val lineData = LineData(actualDataSet, futureDataSet, historyDataSet)
        lineChart.data = lineData

        // Customize chart
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val date = Date(value.toLong())
                    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }
            }
        }
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 1f // Since values are normalized to [0, 1]
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$%.2f".format(value * maxValue) // Denormalize for display
                }
            }
        }
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }
}