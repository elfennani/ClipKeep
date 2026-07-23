package com.elfen.clipkeep.domain.model

data class EditingClipPart(
    val id: Long,
    val name: String?,
    val startMs: Long,
    val finishMs: Long,
    val enabled: Boolean = true
) {
    companion object {
        val samples: List<EditingClipPart> = listOf(
            EditingClipPart(
                id = 1,
                name = "Opening scene",
                startMs = 0L,
                finishMs = 30_000L,
            ),
            EditingClipPart(
                id = 2,
                name = "Funny moment",
                startMs = 92_500L,
                finishMs = 118_000L,
            ),
            EditingClipPart(
                id = 3,
                name = null,
                startMs = 245_000L,
                finishMs = 310_000L,
            ),
            EditingClipPart(
                id = 4,
                name = null,
                startMs = 480_000L,
                finishMs = 525_000L,
                enabled = false,
            ),
        )
    }
}
