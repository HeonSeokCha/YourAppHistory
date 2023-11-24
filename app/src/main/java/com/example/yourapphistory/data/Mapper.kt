package com.example.yourapphistory.data

import com.example.yourapphistory.common.toLocalDateTime
import com.example.yourapphistory.common.toMillis
import com.example.yourapphistory.data.db.entity.AppInfoEntity
import com.example.yourapphistory.data.db.entity.AppUsageEntity
import com.example.yourapphistory.domain.model.AppInfo
import com.example.yourapphistory.domain.model.AppUsageInfo

fun AppInfo.toEntity(): AppInfoEntity {
    return AppInfoEntity(
        packageName = this.packageName,
        label = this.label,
        icon = this.icon
    )
}

fun AppInfoEntity.toAppInfo(): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        label = this.label,
        icon = this.icon
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