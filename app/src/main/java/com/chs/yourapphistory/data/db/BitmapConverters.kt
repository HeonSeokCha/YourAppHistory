package com.chs.yourapphistory.data.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class BitmapConverters {

    @TypeConverter
    fun toByteArray(icon: Bitmap?): ByteArray {
        val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        icon?.compress(
            Bitmap.CompressFormat.PNG,
            100,
            outputStream
        )
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toDrawable(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size
        )
    }
}