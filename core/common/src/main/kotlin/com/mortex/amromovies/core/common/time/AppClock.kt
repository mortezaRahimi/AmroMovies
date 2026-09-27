package com.mortex.amromovies.core.common.time

fun interface AppClock {
    fun nowEpochMillis(): Long
}

object SystemAppClock : AppClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
