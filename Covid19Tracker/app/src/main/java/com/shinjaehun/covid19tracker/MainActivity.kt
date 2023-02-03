package com.shinjaehun.covid19tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.robinhood.ticker.TickerUtils
import com.shinjaehun.covid19tracker.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val TAG = "MainActivity"
private const val ALL_STATES = "All (Nationwide)"

class MainActivity : AppCompatActivity() {
    private lateinit var currentlyShownData: List<Covid19Data>
    private lateinit var adapter: Covid19SparkAdapter
    private lateinit var perStateDailyData: Map<String, List<Covid19Data>>
    private lateinit var nationalDailyData: List<Covid19Data>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.app_description)

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

                setupEventListeners()

                nationalDailyData = nationalData.reversed()
                Log.i(TAG, "Update graph with national data")
                updateDisplayWithData(nationalDailyData)

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

                updateSpinnerWithStateData(perStateDailyData.keys)

            }
        })
    }

    private fun updateSpinnerWithStateData(stateNames: Set<String>) {
        val stateAbbreviationList = stateNames.toMutableList()
        stateAbbreviationList.sort()
        stateAbbreviationList.add(0, ALL_STATES)

        binding.spinnerSelect.setItems(stateAbbreviationList)
        binding.spinnerSelect.setOnSpinnerItemSelectedListener<String> { oldIdx, oldItem, newIdx, newItem ->
            val selectedData = perStateDailyData[newItem] ?: nationalDailyData
            updateDisplayWithData(selectedData)
//            Log.d("hello", "$newItem is selected! (previous: $oldItem)")
        }
    }

    private fun setupEventListeners() {
        binding.tickerView.setCharacterLists(TickerUtils.provideNumberList())

        // add a listener for the user scrubbing on the chart
        binding.sparkView.isScrubEnabled = true
        binding.sparkView.setScrubListener { itemData ->
            if (itemData is Covid19Data) {
                updateInfoForDate(itemData)
            }
        }

        // respond to radio button selected events
        binding.radioGroupTimeSelection.setOnCheckedChangeListener { _, checkedId ->
            adapter.daysAgo = when (checkedId) {
                R.id.radioButtonWeek -> TimeScale.WEEK
                R.id.radioButtonMonth -> TimeScale.MONTH
                else -> TimeScale.MAX
            }
            adapter.notifyDataSetChanged()
        }
        binding.radioGroupMetricSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonNegative -> updateDisplayMetric(Metric.NEGATIVE)
                R.id.radioButtonPositive -> updateDisplayMetric(Metric.POSITIVE)
                R.id.radioButtonDeath -> updateDisplayMetric(Metric.DEATH)
            }

        }
    }

    private fun updateDisplayMetric(metric: Metric) {
        // update the color of the chart
        val colorRes = when (metric) {
            Metric.NEGATIVE -> R.color.colorNegative
            Metric.POSITIVE -> R.color.colorPositive
            Metric.DEATH -> R.color.colorDeath
        }
        @ColorInt val colorInt = ContextCompat.getColor(this, colorRes)
        binding.sparkView.lineColor = colorInt
        binding.tickerView.setTextColor(colorInt)

        // update the metirc on the adapter
        adapter.metric = metric
        adapter.notifyDataSetChanged()

        // reset number and date shown in the bottom text views
        updateInfoForDate(currentlyShownData.last())
    }

    private fun updateDisplayWithData(dailyData: List<Covid19Data>) {
        currentlyShownData = dailyData

        // create a new sparkadapter with the data
        adapter = Covid19SparkAdapter(dailyData)
        binding.sparkView.adapter = adapter
        // update radio buttons to select the positive cases and max time by default
        // display metric for the most recent date
        binding.radioButtonPositive.isChecked = true
        binding.radioButtonMax.isChecked = true

//        updateInfoForDate(dailyData.last())
        updateDisplayMetric(Metric.POSITIVE)
    }

    private fun updateInfoForDate(covid19Data: Covid19Data) {
        val numCases = when (adapter.metric) {
            Metric.NEGATIVE -> covid19Data.negativeIncrease
            Metric.POSITIVE -> covid19Data.positiveIncrease
            Metric.DEATH -> covid19Data.deathIncrease
        }

        binding.tickerView.text = NumberFormat.getInstance().format(numCases)
        val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        binding.tvDateLabel.text = outputDateFormat.format(covid19Data.dateChecked)
    }
}