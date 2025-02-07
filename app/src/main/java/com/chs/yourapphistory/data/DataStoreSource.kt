package com.chs.yourapphistory.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreSource(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun <T> getData(key: Preferences.Key<T>): T? =
        dataStore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }.map { preferences ->
            preferences[key]
        }.first()

    suspend fun <T> updateData(
        key: Preferences.Key<T>,
        value: T
    ) = dataStore.edit { it[key] = value }
}