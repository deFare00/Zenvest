package com.dicoding.zenvest.retrofit

import com.dicoding.zenvest.data.CoinData
import retrofit2.Response
import retrofit2.http.GET

interface BtcService {
    @GET("prices/btc")
    suspend fun getCoinData(): Response<CoinData>
}