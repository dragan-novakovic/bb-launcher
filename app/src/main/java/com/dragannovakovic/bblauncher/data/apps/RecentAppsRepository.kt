package com.dragannovakovic.bblauncher.data.apps

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val RecentAppLimit = 6
private const val RecentAppSeparator = "\n"
private val Context.launcherDataStore by preferencesDataStore(name = "launcher_state")

class RecentAppsRepository(context: Context) {
    private val dataStore = context.applicationContext.launcherDataStore

    val recentComponentNames: Flow<List<String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e(LogTag, "Unable to read recent applications.", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            decodeRecentComponents(preferences[RecentAppsKey].orEmpty())
        }

    suspend fun record(componentName: String) {
        dataStore.edit { preferences ->
            val updatedComponents = buildList {
                add(componentName)
                addAll(
                    decodeRecentComponents(preferences[RecentAppsKey].orEmpty())
                        .filterNot { recentComponent -> recentComponent == componentName },
                )
            }.take(RecentAppLimit)

            preferences[RecentAppsKey] = encodeRecentComponents(updatedComponents)
        }
    }

    private companion object {
        const val LogTag = "RecentAppsRepository"
        val RecentAppsKey: Preferences.Key<String> =
            stringPreferencesKey("recent_app_ids_v2")
    }
}

internal fun encodeRecentComponents(componentNames: List<String>): String =
    componentNames.joinToString(separator = RecentAppSeparator)

internal fun decodeRecentComponents(value: String): List<String> =
    value
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .take(RecentAppLimit)
        .toList()
