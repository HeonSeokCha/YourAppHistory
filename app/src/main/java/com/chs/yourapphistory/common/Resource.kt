package com.chs.yourapphistory.common

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    class Success<T>(val data: T) : Resource<T>()
    class Error(val exception: String) : Resource<Nothing>()
}