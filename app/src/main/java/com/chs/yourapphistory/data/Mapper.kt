package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo

fun AppInfoEntity.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        label = this.label,
        icon = icon
    )
}

fun AppUsageInfo.toEntity(): AppUsageEntity {
    return AppUsageEntity(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime.toMillis(),
        endUseTime = this.endUseTime.toMillis()
    )
}

fun AppUsageEntity.toAppUsageInfo(): AppUsageInfo {
    return AppUsageInfo(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime.toLocalDateTime(),
        endUseTime = this.endUseTime.toLocalDateTime()
    )
}