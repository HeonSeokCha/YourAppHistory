package com.chs.yourapphistory.data

import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.domain.model.AppInfo

fun AppInfoEntity.toAppInfo(): AppInfo {
    return AppInfo(
        packageName = this.packageName,
        label = this.label,
    )
}
