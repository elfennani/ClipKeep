package com.elfen.clipkeep.domain.model

sealed class VideoError : Exception() {
    data object FileNotFound : VideoError()
    data object ReadingFailed : VideoError()
    data object InvalidType : VideoError()
}

sealed class AppError : Exception() {
    data object NotFound : AppError()
}