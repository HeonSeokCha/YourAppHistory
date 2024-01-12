package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.domain.model.AppForegroundUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo

fun AppInfoEntity.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        label = this.label,
        icon = icon
    )
}

fun AppUsageEntity.toAppUsageInfo(): AppUsageInfo {
    return AppUsageInfo(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime.toLocalDateTime(),
        endUseTime = this.endUseTime.toLocalDateTime()
    )
}

fun AppNotifyInfoEntity.toAppNotifyInfo(): AppNotifyInfo {
    return AppNotifyInfo(
        packageName = this.packageName,
        notifyTime = this.notifyTime.toLocalDateTime()
    )
}

fun AppForegroundUsageEntity.toAppForegroundUsageInfo(): AppForegroundUsageInfo {
    return AppForegroundUsageInfo(
        packageName = this.packageName,
        beginTime = this.beginUseTime.toLocalDateTime(),
        endTime = this.endUseTime.toLocalDateTime()
    )
}
