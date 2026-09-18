package com.edusphere.urduassistant

import android.content.Context
import android.content.Intent
import android.net.Uri

object SmsRouter {

    data class Result(val handled: Boolean, val message: String)

    fun composeToNumber(context: Context, phone: String, body: String): Result {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) return Result(false, "Phone number nahi mila.")
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(cleanPhone)}")
                if (body.isNotBlank()) putExtra("sms_body", body)
            }
            context.startActivity(intent)
            Result(true, "SMS tayyar hai. Send karne se pehle review kar lena.")
        } catch (_: Exception) {
            Result(false, "SMS app open nahi ho saki.")
        }
    }
}
