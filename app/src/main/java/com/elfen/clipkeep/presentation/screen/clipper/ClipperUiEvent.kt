package com.elfen.clipkeep.presentation.screen.clipper

sealed interface ClipperUiEvent {
    data class AddClip(val start: Long) : ClipperUiEvent
    data class SetClipStartTime(val id: Long, val time: Long) : ClipperUiEvent
    data class SetClipEndTime(val id: Long, val time: Long) : ClipperUiEvent
    data class TogglePart(val id: Long) : ClipperUiEvent
    data class UpdatePartName(val id: Long, val name: String?) : ClipperUiEvent
    data object Render : ClipperUiEvent
}