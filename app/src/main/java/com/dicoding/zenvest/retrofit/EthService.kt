package com.dicoding.zenvest.retrofit

import com.dicoding.zenvest.data.CoinData
import retrofit2.Response
import retrofit2.http.GET

interface EthService {
    @GET("prices/eth")
    suspend fun getCoinData(): Response<CoinData>
}