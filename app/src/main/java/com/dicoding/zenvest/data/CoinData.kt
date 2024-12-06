package com.dicoding.zenvest.data

data class CoinData(
    val coin: String?,
    val coin_symbol: String,
    val current_price: String?,
    val marketcap: String,
    val percentage: String?,
    val volume: String,
    val yearly_percentage: Int,
    val prediction_chart: PredictionChart
)

data class PredictionChart(
    val actual_price: List<PriceData>?,
    val future_prediction: List<PriceData>?,
    val history_predicted: List<PriceData>?
)

data class PriceData(
    val time: String,
    val value: String
)

