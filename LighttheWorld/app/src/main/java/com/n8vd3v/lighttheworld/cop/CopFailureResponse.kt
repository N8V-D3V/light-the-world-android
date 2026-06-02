package com.n8vd3v.lighttheworld.cop

data class CopFailureResponse<T>(
    val reason: T,
    val message: String,
)
