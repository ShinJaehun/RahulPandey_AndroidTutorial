package com.shinjaehun.covid19tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var perStateDailyData: Map<String, List<Covid19Data>>
    private lateinit var nationalDailyData: List<Covid19Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val gson = GsonBuilder().create()
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covid19Service = retrofit.create(Covid19Service::class.java)

        // fetch the national data
        covid19Service.getNationalData().enqueue(object: Callback<List<Covid19Data>> {
            override fun onFailure(call: Call<List<Covid19Data>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(
                call: Call<List<Covid19Data>>,
                response: Response<List<Covid19Data>>
            ) {
                Log.i(TAG, "onResponse $response")
                val nationalData = response.body()
                if(nationalData == null) {
                    Log.w(TAG, "did not receive a valid response body")
                    return
                }
                nationalDailyData = nationalData.reversed()
                Log.i(TAG, "Update graph with national data")
            }
        })

        // fetch the state data
        covid19Service.getStatesData().enqueue(object: Callback<List<Covid19Data>> {
            override fun onFailure(call: Call<List<Covid19Data>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(
                call: Call<List<Covid19Data>>,
                response: Response<List<Covid19Data>>
            ) {
                Log.i(TAG, "onResponse $response")
                val statesData = response.body()
                if(statesData == null) {
                    Log.w(TAG, "did not receive a valid response body")
                    return
                }
                perStateDailyData = statesData.reversed().groupBy { it.state }
                Log.i(TAG, "Update spinner with state name")
            }
        })
    }
}