package com.example.zenvest.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.navigationbarkotlin.R
import com.example.navigationbarkotlin.BtcActivity
import com.example.navigationbarkotlin.DetailsActivity
import com.example.navigationbarkotlin.EthActivity
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment() {
    private var dataList: MutableList<CoinData> = ArrayList()
    private var coinSymbols: List<String> = mutableListOf(
        "BTC", "ETH", "SOL", "XRP", "SUI", "ENA", "TIA", "TAO", "OP", "RENDER"
    )
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_news)
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        // Set horizontal layout
        fetchCoinOptionsFromAPI()

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = CardAdapter(requireContext(), dataList)
        recyclerView.adapter = adapter

        // Search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    val filteredCoin = dataList.find { it.coin_symbol.equals(query, true) }
                    if (filteredCoin != null) {
                        when (filteredCoin.coin_symbol.uppercase()) {
                            "BTC" -> startActivity(Intent(requireContext(), BtcActivity::class.java))
                            "ETH" -> startActivity(Intent(requireContext(), EthActivity::class.java))
                            else -> {
                                val intent = Intent(requireContext(), DetailsActivity::class.java)
                                intent.putExtra("symbol", filteredCoin.coin_symbol)
                                startActivity(intent)
                            }
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: implement dynamic filtering while typing
                return true
            }
        })

        return view
    }

    private fun fetchCoinOptionsFromAPI() {
        for (coinSymbol in coinSymbols) {
            val url =
                "https://zenvest-prices-apis-747657276300.asia-southeast2.run.app/prices/$coinSymbol"

            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response: JSONObject ->
                    try {
                        val name = response.getString("coin")
                        val symbol = response.getString("coin_symbol")
                        val currentPrice = response.getString("current_price")
                        val marketCap = response.optString("marketcap", "N/A")
                        val percentage = response.getString("percentage")
                        val volume = response.optString("volume", "N/A")
                        val yearlyPercentage = response.optInt("yearly_percentage", 0)

                        // Parse Prediction Chart
                        val actualPrices = mutableListOf<PriceData>()
                        val futurePredictions = mutableListOf<PriceData>()
                        val historyPredicted = mutableListOf<PriceData>()

                        if (response.has("prediction_chart")) {
                            val predictionChart = response.getJSONObject("prediction_chart")

                            // Parse actual prices
                            if (predictionChart.has("actual_price")) {
                                val actualPriceArray = predictionChart.getJSONArray("actual_price")
                                for (i in 0 until actualPriceArray.length()) {
                                    val data = actualPriceArray.getJSONObject(i)
                                    actualPrices.add(PriceData(data.getString("time"), data.getString("value")))
                                }
                            }

                            // Parse future predictions
                            if (predictionChart.has("future_prediction")) {
                                val futurePredictionArray = predictionChart.getJSONArray("future_prediction")
                                for (i in 0 until futurePredictionArray.length()) {
                                    val data = futurePredictionArray.getJSONObject(i)
                                    futurePredictions.add(PriceData(data.getString("time"), data.getString("value")))
                                }
                            }

                            // Parse history predicted
                            if (predictionChart.has("history_predicted")) {
                                val historyPredictedArray = predictionChart.getJSONArray("history_predicted")
                                for (i in 0 until historyPredictedArray.length()) {
                                    val data = historyPredictedArray.getJSONObject(i)
                                    historyPredicted.add(PriceData(data.getString("time"), data.getString("value")))
                                }
                            }
                        }

                        // Ensure non-null PredictionChart
                        val predictionChart = PredictionChart(
                            actual_price = actualPrices,
                            future_prediction = futurePredictions,
                            history_predicted = historyPredicted
                        )

                        // Create CoinData
                        val coinData = CoinData(
                            coin = name,
                            coin_symbol = symbol,
                            current_price = currentPrice,
                            marketcap = marketCap,
                            percentage = percentage,
                            volume = volume,
                            yearly_percentage = yearlyPercentage,
                            prediction_chart = predictionChart
                        )

                        dataList.add(coinData)

                        adapter.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                { error: VolleyError -> error.printStackTrace() }
            )

            Volley.newRequestQueue(requireContext()).add(request)
        }
    }
}