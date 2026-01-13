package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.model.AppInfoData
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo

fun AppInfoEntity.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        icon = icon,
        label = this.label,
    )
}

fun AppInfoData.toAppInfo(icon: Bitmap?): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        icon = icon,
        label = this.label,
    )
}

fun AppInfoData.toAppSimpleInfo(totalUsedInfo: Long): AppTotalUsageInfo {
    return AppTotalUsageInfo(
        packageName = this.packageName,
        label = this.label,
        totalUsedInfo = totalUsedInfo
    )
}