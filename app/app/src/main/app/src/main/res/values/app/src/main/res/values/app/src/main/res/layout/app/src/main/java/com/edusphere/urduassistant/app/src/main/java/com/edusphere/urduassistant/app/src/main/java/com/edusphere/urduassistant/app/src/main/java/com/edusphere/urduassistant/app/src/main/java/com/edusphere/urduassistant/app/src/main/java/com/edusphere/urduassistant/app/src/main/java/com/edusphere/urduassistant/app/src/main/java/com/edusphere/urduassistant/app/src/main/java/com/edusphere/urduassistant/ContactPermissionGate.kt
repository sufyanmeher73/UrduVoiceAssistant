package com.edusphere.urduassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

/** Centralized guard for contact-backed commands. */
object ContactPermissionGate {
    const val REQUEST_CODE = 12

    fun ensure(context: Context): Boolean {
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        (context as? Activity)?.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE)
        return false
    }
}
