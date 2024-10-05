package com.muhaimen.arenax.api

import com.muhaimen.arenax.dataClasses.ApiResponse

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
   @GET("/gamesDictionary/games") // Adjust this path to match your backend route
   fun getGames(): Call<List<ApiResponse>>
}
