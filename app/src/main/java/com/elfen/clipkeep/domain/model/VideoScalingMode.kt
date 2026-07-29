package com.elfen.clipkeep.domain.model

enum class VideoScalingMode {
    SCALE_TO_FIT,
    SCALE_TO_9_16,
    SCALE_TO_FILL
}

fun VideoScalingMode.next(): VideoScalingMode {
    return when (this) {
        VideoScalingMode.SCALE_TO_FIT -> VideoScalingMode.SCALE_TO_9_16
        VideoScalingMode.SCALE_TO_9_16 -> VideoScalingMode.SCALE_TO_FILL
        VideoScalingMode.SCALE_TO_FILL -> VideoScalingMode.SCALE_TO_FIT
    }
}