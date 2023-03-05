package com.shinjaehun.covid19tracker

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter

class Covid19SparkAdapter(private val dailyData: List<Covid19Data>): SparkAdapter() {

    var metric = Metric.POSITIVE
    var daysAgo = TimeScale.MAX

    override fun getY(index: Int): Float {
        val chosenDayData = dailyData[index]
//        return chosenDayData.positiveIncrease.toFloat()
        return when (metric) {
            Metric.NEGATIVE -> chosenDayData.negativeIncrease.toFloat()
            Metric.POSITIVE -> chosenDayData.positiveIncrease.toFloat()
            Metric.DEATH -> chosenDayData.deathIncrease.toFloat()
        }
    }

    override fun getItem(index: Int) = dailyData[index]

    override fun getCount(): Int = dailyData.size

    override fun getDataBounds(): RectF {
        val bounds = super.getDataBounds()
        if (daysAgo != TimeScale.MAX) {
            bounds.left = count - daysAgo.numDays.toFloat()
        }
        return bounds
    }
}
