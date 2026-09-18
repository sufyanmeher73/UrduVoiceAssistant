package com.edusphere.urduassistant

import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SystemActionRouter {
    enum class Action { SETTINGS, CAMERA, FILES, TIME }

    data class Result(val handled: Boolean, val message: String)

    fun execute(context: Context, action: Action): Result = runCatching {
        when (action) {
            Action.SETTINGS -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Result(true, "سیٹنگز کھول رہا ہوں۔")
            }
            Action.CAMERA -> {
                context.startActivity(Intent("android.media.action.IMAGE_CAPTURE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Result(true, "کیمرہ کھول رہا ہوں۔")
            }
            Action.FILES -> {
                context.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Result(true, "فائل سلیکٹر کھول رہا ہوں۔")
            }
            Action.TIME -> {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                Result(true, "ابھی وقت $time ہے۔")
            }
        }
    }.getOrElse { Result(false, "System action execute nahi ho saki.") }
}
