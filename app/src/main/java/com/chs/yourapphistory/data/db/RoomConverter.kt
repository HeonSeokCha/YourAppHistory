package com.chs.yourapphistory.data.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class RoomConverter {
    @TypeConverter
    fun toByteArray(bitmap : Bitmap?): ByteArray? {
        if (bitmap == null) return null
        return ByteArrayOutputStream().run {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            this.toByteArray()
        }
    }

    @TypeConverter
    fun toBitmap(bytes : ByteArray?) : Bitmap? {
        if (bytes == null) return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}