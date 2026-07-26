package com.elfen.clipkeep.domain.model

data class EditingClipPart(
    val id: Long,
    val name: String?,
    val startMs: Long,
    val finishMs: Long,
    val enabled: Boolean = true,
    val crop: Crop
) {
    companion object {
        val samples: List<EditingClipPart> = listOf(
            EditingClipPart(
                id = 1,
                name = "Opening scene",
                startMs = 0L,
                finishMs = 30_000L,
                crop = Crop(
                    x = 120f,
                    y = 80f,
                    width = 1280f,
                    height = 720f,
                ),
            ),
            EditingClipPart(
                id = 2,
                name = "Funny moment",
                startMs = 92_500L,
                finishMs = 118_000L,
                crop = Crop(
                    x = 240f,
                    y = 135f,
                    width = 960f,
                    height = 540f,
                ),
            ),
            EditingClipPart(
                id = 3,
                name = null,
                startMs = 245_000L,
                finishMs = 310_000L,
                crop = Crop(
                    x = 80f,
                    y = 120f,
                    width = 1440f,
                    height = 810f,
                ),
            ),
            EditingClipPart(
                id = 4,
                name = null,
                startMs = 480_000L,
                finishMs = 525_000L,
                enabled = false,
                crop = Crop(
                    x = 360f,
                    y = 200f,
                    width = 720f,
                    height = 480f,
                ),
            ),
        )
    }
}
