package com.example.zenvest.Sizing

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.navigationbarkotlin.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONException
import java.text.NumberFormat

class SizingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var numCoin: TextView
    private lateinit var suggestedTitle: TextView
    private lateinit var qtyMoney: EditText
    private lateinit var minButton: Button
    private lateinit var plusButton: Button
    private lateinit var createSizing: Button
    private lateinit var textViewContainer1: LinearLayout
    private lateinit var textViewContainer2: LinearLayout
    private lateinit var textViewContainer3: LinearLayout

    private var num = 2
    private val name = mutableListOf<String>()
    private lateinit var adapter: MyAdapter
    private val url = "https://zenvest-prices-apis-747657276300.asia-southeast2.run.app/prices"

    private fun updateRecyclerView(num: Int) {
        name.clear()
        for (i in 1..num) {
            name.add("Pilihan $i")
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
            val view = inflater.inflate(R.layout.fragment_sizing, container, false)

            recyclerView = view.findViewById(R.id.rv)
            numCoin = view.findViewById(R.id.num_coin)
            minButton = view.findViewById(R.id.min_button)
            plusButton = view.findViewById(R.id.plus_button)
            qtyMoney = view.findViewById(R.id.qty_money)
            textViewContainer1 = view.findViewById(R.id.textViewContainer1)
            textViewContainer2 = view.findViewById(R.id.textViewContainer2)
            textViewContainer3 = view.findViewById(R.id.textViewContainer3)
            suggestedTitle = view.findViewById(R.id.suggested_title)
            createSizing = view.findViewById(R.id.create_sizing)

            updateRecyclerView(num)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            adapter = MyAdapter(requireContext(), name)
            recyclerView.adapter = adapter
            val selectedItems = adapter.getSelectedItems()

            fetchCoinOptionsFromAPI()

            minButton.setOnClickListener {
    if (num > 2) {
        num--
        numCoin.text = num.toString()
        updateRecyclerView(num)
        recyclerView.adapter = adapter
        textViewContainer1.removeAllViews()
        textViewContainer2.removeAllViews()
        textViewContainer3.removeAllViews()
        selectedItems.clear()
        selectedItems.putAll(adapter.getSelectedItems())
    } else {
        Toast.makeText(context, "Minimum coin is $num", Toast.LENGTH_SHORT).show()
    }
        }

    plusButton.setOnClickListener {
        if (num <= 5) {
            num++
            numCoin.text = num.toString()
            updateRecyclerView(num)
            recyclerView.adapter = adapter
            textViewContainer1.removeAllViews()
            textViewContainer2.removeAllViews()
            textViewContainer3.removeAllViews()
            selectedItems.clear()
            selectedItems.putAll(adapter.getSelectedItems())
        } else {
            Toast.makeText(context, "Maximum coins are $num", Toast.LENGTH_SHORT).show()
        }
    }

    createSizing.setOnClickListener {
        suggestedTitle.visibility = View.VISIBLE
        textViewContainer1.removeAllViews()
        textViewContainer2.removeAllViews()
        textViewContainer3.removeAllViews()
        addTextViews(selectedItems)
    }
    return view
    }

    private fun updatePieChart(pieEntries: ArrayList<PieEntry>) {
        val pieChart = requireView().findViewById<PieChart>(R.id.pie_chart)

                val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        pieDataSet.valueTextSize = 12f
        pieDataSet.valueTextColor = Color.BLACK

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        pieChart.visibility = View.VISIBLE
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun addTextViews(selectedItems: HashMap<Int, String>) {
        val marketCapMap = mutableMapOf<Int, Float>()
        var total = 0f

        val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        for ((position, value) in selectedItems) {
            val textView1 = TextView(context).apply {
                text = value
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.BLACK)
                this.layoutParams = layoutParams
            }
            textViewContainer1.addView(textView1)

            fetchCoinMarketCapFromAPI(value) { marketCap ->
                try {
                    val parsedValue = marketCap.replace(",", "").toFloat()
                    marketCapMap[position] = parsedValue
                    total += parsedValue

                    if (marketCapMap.size == selectedItems.size) {
                        updateTextViewContainers(marketCapMap, total, selectedItems, layoutParams)
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateTextViewContainers(
            marketCapMap: Map<Int, Float>, total: Float,
            selectedItems: HashMap<Int, String>, layoutParams: LinearLayout.LayoutParams
    ) {
        textViewContainer2.removeAllViews()
        textViewContainer3.removeAllViews()

        val pieEntries = ArrayList<PieEntry>()
        val qtyOfMoney = qtyMoney.text.toString().toFloat()

        for ((position, value) in selectedItems) {
            val marketCap = marketCapMap[position] ?: 0f
            val percentage = if (total != 0f) (marketCap / total) * 100 else 0f
            val money = (marketCap / total) * qtyOfMoney
            val formattedMoney = NumberFormat.getInstance().format(money.toInt())
            val formattedPercentage = "%.2f".format(percentage)

            val textView2 = TextView(context).apply {
                text = "$formattedPercentage%"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.BLACK)
                this.layoutParams = layoutParams
                gravity = Gravity.END
            }
            textViewContainer2.addView(textView2)

            val textView3 = TextView(context).apply {
                text = "Rp$formattedMoney,-"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.BLACK)
                this.layoutParams = layoutParams
                gravity = Gravity.END
            }
            textViewContainer3.addView(textView3)

            pieEntries.add(PieEntry(percentage, value))
        }
        updatePieChart(pieEntries)
    }

    private fun fetchCoinOptionsFromAPI() {
        val request = JsonArrayRequest(Request.Method.GET, url, null,
                { response ->
        try {
            val coinNames = mutableListOf<String>()
            for (i in 0 until response.length()) {
                val coin = response.getJSONObject(i)
                coinNames.add("${coin.getString("coin")} (${coin.getString("coin_symbol")})")
            }
            adapter.updateItems(coinNames)
            adapter.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
            },
        { error -> error.printStackTrace() }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchCoinMarketCapFromAPI(selectedItem: String, callback: (String) -> Unit) {
        val regex = "\\((.*?)\\)".toRegex()
        val coinSymbol = regex.find(selectedItem)?.groupValues?.get(1) ?: ""

        val request = JsonObjectRequest(Request.Method.GET, "$url/$coinSymbol", null,
                { response ->
        try {
            val marketCap = response.getString("marketcap")
            callback(marketCap)
        } catch (e: JSONException) {
            e.printStackTrace()
            callback("Market Cap Error")
        }
            },
        { error ->
                error.printStackTrace()
            callback("Network Error: ${error.message}")
        }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }
}
