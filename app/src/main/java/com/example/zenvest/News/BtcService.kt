package com.example.zenvest.News

import com.example.zenvest.Fragments.CoinData
import retrofit2.Response
import retrofit2.http.GET

interface BtcService {
    @GET("prices/btc")
    suspend fun getCoinData(): Response<CoinData>
}