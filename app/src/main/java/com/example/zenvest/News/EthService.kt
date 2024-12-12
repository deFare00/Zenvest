package com.example.zenvest.News

import com.example.zenvest.Fragments.CoinData
import retrofit2.Response
import retrofit2.http.GET

interface EthService {
    @GET("prices/eth")
    suspend fun getCoinData(): Response<CoinData>
}