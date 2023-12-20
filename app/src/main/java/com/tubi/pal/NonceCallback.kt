package com.tubi.pal

interface NonceCallback {
    fun onSuccess(value: String?)
    fun onFailure(exception: Exception?)
}