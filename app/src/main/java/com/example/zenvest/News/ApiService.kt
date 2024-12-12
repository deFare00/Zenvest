package com.example.zenvest.News

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("news/{topic}")
    suspend fun getNews(@Path("topic") topic: String): Response<List<NewsItem>>
}