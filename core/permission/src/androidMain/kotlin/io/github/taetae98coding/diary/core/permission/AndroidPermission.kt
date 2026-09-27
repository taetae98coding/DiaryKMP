package io.github.taetae98coding.diary.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

public fun Permission.toAndroidPermissionList(): List<String> =
    when (this) {
        Permission.NOTIFICATION -> listOf(Manifest.permission.POST_NOTIFICATIONS)

        Permission.LOCATION ->
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )

        Permission.CAMERA -> listOf(Manifest.permission.CAMERA)
    }

// 정확도가 다른 권한을 함께 요청하면 시스템이 그중 하나만 허용할 수 있으므로 하나라도 허용되면 허용으로 본다.
public fun Context.isPermissionGranted(permission: Permission): Boolean = permission.toAndroidPermissionList().any { name -> checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED }
