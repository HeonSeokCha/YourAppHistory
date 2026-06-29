package com.chs.yourapphistory.data

import android.graphics.Bitmap
import com.chs.yourapphistory.data.db.entity.AppForegroundUsageEntity
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.db.entity.IncompleteAppUsageEntity
import com.chs.yourapphistory.data.model.AppInfoData
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import com.chs.yourapphistory.domain.model.InCompleteAppUsageInfo

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

fun AppUsageInfo.toAppUsageEntity(beginQueryTime: Long, createTime: Long): AppUsageEntity {
    return AppUsageEntity(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime,
        endUseTime = this.endUseTime,
        beginQueryTime = beginQueryTime,
        createTime = createTime
    )
}

fun AppUsageInfo.toAppForegroundUsageEntity(beginQueryTime: Long, createTime: Long): AppForegroundUsageEntity {
    return AppForegroundUsageEntity(
        packageName = this.packageName,
        beginUseTime = this.beginUseTime,
        endUseTime = this.endUseTime,
        beginQueryTime = beginQueryTime,
        createTime = createTime
    )
}

fun InCompleteAppUsageInfo.toInCompleteAppUsageEntity(createTime: Long): IncompleteAppUsageEntity {
    return IncompleteAppUsageEntity(
        packageName = this.packageName,
        className = this.className,
        usageType = this.usageType,
        beginUseTime = this.beginUseTime,
        createTime = createTime
    )
}