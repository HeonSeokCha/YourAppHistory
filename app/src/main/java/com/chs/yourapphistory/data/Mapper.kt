package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.domain.model.AppInfo

fun AppInfoEntity.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        icon = icon,
        label = this.label,
    )
}
