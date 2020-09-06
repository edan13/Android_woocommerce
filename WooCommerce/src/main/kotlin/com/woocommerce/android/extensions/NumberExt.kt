package com.woocommerce.android.extensions

import kotlin.math.roundToInt

fun Float.formatToString(): String {
    val int = this.roundToInt()
    return if (this != int.toFloat()) {
        this.toString()
    } else {
        int.toString()
    }
}
