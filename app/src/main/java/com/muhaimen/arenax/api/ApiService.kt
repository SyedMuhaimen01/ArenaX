package com.muhaimen.arenax.api


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
   @GET("/gamesDictionary/games")
   fun getGames(@Query("userId") userId: String): Call<List<ApiResponse>>
}

