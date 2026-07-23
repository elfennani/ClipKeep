package com.elfen.clipkeep.utils

fun Long.msToText(): String {
    val totalSeconds = this / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    val hourString = hours.toString().padStart(2, '0')
    val minString = minutes.toString().padStart(2, '0')
    val secString = seconds.toString().padStart(2, '0')

    return if (hours > 0) {
        "$hourString:$minString:$secString"
    } else {
        "$minString:$secString"
    }
}