package com.elfen.clipkeep.domain.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.elfen.clipkeep.data.local.DataStorePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Settings(
    val fullscreen: Boolean
) {
    companion object {
        val FULLSCREEN_KEY = booleanPreferencesKey("USE_FULLSCREEN")

        suspend fun setFullscreen(dataStore: DataStorePreferences, fullscreen: Boolean) {
            dataStore.edit {
                it[FULLSCREEN_KEY] = fullscreen
            }
        }

        fun fromDataStore(preferences: Preferences): Settings {
            return Settings(
                fullscreen = preferences[FULLSCREEN_KEY] ?: false
            )
        }
    }
}

val DataStorePreferences.settings
    get() = data.map {
        Settings.fromDataStore(it)
    }
