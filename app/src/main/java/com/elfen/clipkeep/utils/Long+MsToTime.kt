package com.elfen.clipkeep.utils

fun Long.msToText(): String {
    val minutes = this / 60_000;
    val seconds = (this % 60_000) / 1000;

    val minString = minutes.toString().padStart(2, '0')
    val secString = seconds.toString().padStart(2, '0')

    return "$minString:$secString";
}