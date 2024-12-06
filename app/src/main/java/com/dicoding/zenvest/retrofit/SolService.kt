package com.dicoding.zenvest.retrofit

import com.dicoding.zenvest.data.CoinData
import retrofit2.Response
import retrofit2.http.GET

interface SolService {
    @GET("prices/sol")
    suspend fun getCoinData(): Response<CoinData>
}