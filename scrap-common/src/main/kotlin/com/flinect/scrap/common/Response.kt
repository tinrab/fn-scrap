package com.flinect.scrap.common

sealed class Response<out S : Any> {
    abstract fun <T : Any> map(f: (S) -> T): Response<T>
}

data class Success<out S : Any>(val value: S) : Response<S>() {
    override fun <T : Any> map(f: (S) -> T): Response<T> {
        return Success(f(value))
    }
}

data class Failure(val code: String, val message: String? = null) : Response<Nothing>() {
    override fun <T : Any> map(f: (Nothing) -> T): Response<T> {
        return this
    }
}
