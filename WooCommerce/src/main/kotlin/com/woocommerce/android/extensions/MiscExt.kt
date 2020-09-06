package com.woocommerce.android.extensions

inline fun <T> T.takeIfNotEqualTo(other: T?, block: (T) -> Unit) {
    if (this != other)
        block(this)
}
