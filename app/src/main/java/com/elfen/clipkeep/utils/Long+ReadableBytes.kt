package com.elfen.clipkeep.utils

val Long.readableBytes: String
    get() = when {
        this < 1024L -> "${this}B"
        this < 1024L * 1024 -> "${this / 1024}KB"
        this < 1024L * 1024 * 1024 -> "${this / (1024 * 1024)}MB"
        else -> "${this / (1024 * 1024 * 1024)}GB"
    }