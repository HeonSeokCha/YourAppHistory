package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppNotifyInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppNotifyInfo

fun AppInfoEntity.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        label = this.label,
        icon = icon
    )
}

fun AppUsageEntity.toAppUsageInfo(): AppBaseUsageInfo.AppUsageInfo {
    return AppBaseUsageInfo.AppUsageInfo(
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

fun AppForegroundUsageEntity.toAppForegroundUsageInfo(): AppBaseUsageInfo.AppForegroundUsageInfo {
    return AppBaseUsageInfo.AppForegroundUsageInfo(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime.toLocalDateTime(),
        endUseTime = this.endUseTime.toLocalDateTime()
    )
}
