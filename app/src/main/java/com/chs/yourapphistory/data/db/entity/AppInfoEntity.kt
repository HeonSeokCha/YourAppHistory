package com.chs.yourapphistory.data.db.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("appInfo")
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is AppInfoEntity) return false

        return this.packageName == other.packageName
    }
}
