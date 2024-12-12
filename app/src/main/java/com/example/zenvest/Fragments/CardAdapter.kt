package com.example.zenvest.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationbarkotlin.R
import com.example.navigationbarkotlin.BtcActivity
import com.example.navigationbarkotlin.DetailsActivity
import com.example.navigationbarkotlin.EthActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class CardAdapter(
    private val context: Context,
    private val dataList: List<CoinData>
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val coinData = dataList[position]

        // Set data for each card
        holder.title.text = coinData.coin ?: "Unknown Coin"
        holder.code.text = coinData.coin_symbol
        holder.currentValue.text = "$ ${coinData.current_price ?: "N/A"}"

        if (coinData.percentage?.toFloatOrNull() ?: 0f >= 0) {
            holder.percentageChange.text = "▲ ${coinData.percentage ?: "0"}%"
            holder.percentageChange.setTextColor(Color.GREEN)
        } else {
            holder.percentageChange.text = "▼ ${coinData.percentage?.substring(1) ?: "0"}%"
            holder.percentageChange.setTextColor(Color.RED)
        }

        // Setup Line Chart with prediction data if available
        val actualPrices = coinData.prediction_chart.actual_price
        if (!actualPrices.isNullOrEmpty()) {
            setupLineChart(holder.lineChart, actualPrices)
        } else {
            holder.lineChart.visibility = View.INVISIBLE // Hide chart if no data
        }

        // Set click listener to open DetailsActivity
        holder.itemView.setOnClickListener {
            when (coinData.coin_symbol) {
                "BTC" -> {
                    // Navigate to BtcActivity for Bitcoin
                    val intent = Intent(context, BtcActivity::class.java)
                    intent.putExtra("coin_name", coinData.coin)
                    intent.putExtra("current_price", coinData.current_price)
                    intent.putExtra("percentage_change", coinData.percentage)
                    intent.putExtra("volume", coinData.volume)
                    intent.putExtra("marketcap", coinData.marketcap)
                    context.startActivity(intent)
                }
                "ETH" -> {
                    // Navigate to EthActivity for Ethereum
                    val intent = Intent(context, EthActivity::class.java)
                    intent.putExtra("coin_name", coinData.coin)
                    intent.putExtra("current_price", coinData.current_price)
                    intent.putExtra("percentage_change", coinData.percentage)
                    intent.putExtra("volume", coinData.volume)
                    intent.putExtra("marketcap", coinData.marketcap)
                    context.startActivity(intent)
                }
                else -> {
                    // Navigate to DetailsActivity for other coins
                    val intent = Intent(context, DetailsActivity::class.java)
                    intent.putExtra("coin_name", coinData.coin)
                    intent.putExtra("symbol", coinData.coin_symbol)
                    intent.putExtra("currentPrice", coinData.current_price)
                    intent.putExtra("marketCap", coinData.marketcap)
                    intent.putExtra("percentage", coinData.percentage)
                    intent.putExtra("volume", coinData.volume)
                    intent.putExtra("yearlyPercentage", coinData.yearly_percentage)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun setupLineChart(
        lineChart: LineChart,
        chartData: List<PriceData>
    ) {
        val entries = ArrayList<Entry>()

        chartData.forEachIndexed { index, priceData ->
            try {
                val value = priceData.value.toFloatOrNull()
                if (value != null) {
                    entries.add(Entry(index.toFloat(), value))
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace() // Handle invalid format
            }
        }

        if (entries.isEmpty()) {
            lineChart.visibility = View.INVISIBLE // Hide chart if no valid data
            return
        }

        val dataSet = LineDataSet(entries, "Price Trend").apply {
            color = Color.GREEN
            lineWidth = 2f
            setDrawCircles(false) // No dots
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        lineChart.data = LineData(dataSet)

        // Chart styling
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(false)

        lineChart.xAxis.isEnabled = false
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false

        lineChart.invalidate() // Refresh the chart
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val code: TextView = itemView.findViewById(R.id.code)
        val currentValue: TextView = itemView.findViewById(R.id.current_value)
        val percentageChange: TextView = itemView.findViewById(R.id.percentage_change)
        val lineChart: LineChart = itemView.findViewById(R.id.line_chart)
    }
}