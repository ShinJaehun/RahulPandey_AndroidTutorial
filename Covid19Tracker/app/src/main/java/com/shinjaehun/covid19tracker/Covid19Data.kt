package com.shinjaehun.covid19tracker

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Covid19Data(
    // 이렇게 변수명이 같을 때는 SerializedName이 필요 없다네...
//    @SerializedName("dateChecked") val dateChecked: String,
//    @SerializedName("positiveIncrease") val positiveIncrease: String,
//    @SerializedName("negativeIncrease") val negativeIncrease: String,
//    @SerializedName("deathIncrease") val deathIncrease: String,
//    @SerializedName("state") val state: String,

    val dateChecked: Date,
    val positiveIncrease: Int,
    val negativeIncrease: Int,
    val deathIncrease: Int,
    val state: String
) {
}