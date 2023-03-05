package com.shinjaehun.covid19tracker

import retrofit2.Call
import retrofit2.http.GET

interface Covid19Service {
    @GET("us/daily.json")
    fun getNationalData(): Call<List<Covid19Data>>

    @GET("states/daily.json")
    fun getStatesData(): Call<List<Covid19Data>>
}