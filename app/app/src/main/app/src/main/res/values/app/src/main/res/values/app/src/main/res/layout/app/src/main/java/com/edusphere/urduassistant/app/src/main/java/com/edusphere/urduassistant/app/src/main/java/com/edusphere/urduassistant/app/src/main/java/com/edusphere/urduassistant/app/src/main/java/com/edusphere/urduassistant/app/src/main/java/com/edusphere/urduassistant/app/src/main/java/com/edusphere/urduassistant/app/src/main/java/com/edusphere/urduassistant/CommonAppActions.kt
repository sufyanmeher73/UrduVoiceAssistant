package com.edusphere.urduassistant

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri

object CommonAppActions {

    data class Result(val handled: Boolean, val message: String)

    private val packages = mapOf(
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "tiktok" to "com.zhiliaoapp.musically",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "chrome" to "com.android.chrome",
        "calculator" to "com.google.android.calculator",
        "maps" to "com.google.android.apps.maps"
    )

    fun open(context: Context, appName: String): Result {
        val key = normalize(appName)
        val pkg = packages[key] ?: findLaunchablePackage(context, key)
            ?: return Result(false, "$appName installed nahi mila.")
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                ?: return Result(false, "$appName installed nahi mila.")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Result(true, "$appName khol raha hoon.")
        } catch (_: Exception) {
            Result(false, "$appName open nahi ho saka.")
        }
    }

    fun openUrlInChrome(context: Context, url: String): Result {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(packages["chrome"])
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            Result(true, "Chrome mein khol raha hoon.")
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Result(true, "Browser mein khol raha hoon.")
            } catch (_: Exception) {
                Result(false, "Browser open nahi ho saka.")
            }
        }
    }

    private fun findLaunchablePackage(context: Context, key: String): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }

        val exact = candidates.firstOrNull {
            normalize(it.loadLabel(context.packageManager).toString()) == key
        }
        if (exact != null) return exact.activityInfo.packageName

        return candidates.firstOrNull {
            val label = normalize(it.loadLabel(context.packageManager).toString())
            label.contains(key) || key.contains(label)
        }?.activityInfo?.packageName
    }

    private fun normalize(value: String): String =
        value.lowercase().trim().replace(Regex("\\s+"), " ")
}
