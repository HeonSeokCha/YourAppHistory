package com.example.yourapphistory.data.db.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("appInfo")
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null
)