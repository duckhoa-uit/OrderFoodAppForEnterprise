package com.example.orderfoodappforenterprise.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Dish(
    var id: String = "",
    var name: String = "",
    var priceS: Double = 0.0,
    var priceM: Double = 0.0,
    var priceL: Double = 0.0,
    var rated: String = "0",
    var category: String = "",
    var description: String = "",
    var salePercent: Long = 0L,
    var amountS: Long = 0L,
    var amountSsold: Long = 0L,
    var amountM: Long = 0L,
    var amountMsold: Long = 0L,
    var amountL: Long = 0L,
    var amountLsold: Long = 0L,
    var provider: String = ""
): Parcelable {

    //    constructor() {
//        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
//    }

}
