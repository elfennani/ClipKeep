package com.elfen.clipkeep.presentation.screen.clipper

sealed interface ClipperUiEvent {
    data class AddClip(val start: Long) : ClipperUiEvent
    data class SetClipStartTime(val id: Int, val time: Long) : ClipperUiEvent
    data class SetClipEndTime(val id: Int, val time: Long) : ClipperUiEvent
    data object Render : ClipperUiEvent
}