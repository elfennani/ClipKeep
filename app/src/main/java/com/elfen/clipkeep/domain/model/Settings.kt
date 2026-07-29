package com.elfen.clipkeep.domain.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.elfen.clipkeep.data.local.DataStorePreferences
import kotlinx.coroutines.flow.map

data class Settings(
    val scalingMode: VideoScalingMode = VideoScalingMode.SCALE_TO_FIT
) {
    companion object {
        val SCALING_MODE = stringPreferencesKey("SCALING_MODE")

        suspend fun setFullscreen(dataStore: DataStorePreferences, mode: VideoScalingMode) {
            dataStore.edit {
                it[SCALING_MODE] = mode.name
            }
        }

        fun fromDataStore(preferences: Preferences): Settings {
            return Settings(
                scalingMode = preferences[SCALING_MODE]?.let { VideoScalingMode.valueOf(it) }
                    ?: VideoScalingMode.SCALE_TO_FIT
            )
        }
    }
}

val DataStorePreferences.settings
    get() = data.map {
        Settings.fromDataStore(it)
    }
