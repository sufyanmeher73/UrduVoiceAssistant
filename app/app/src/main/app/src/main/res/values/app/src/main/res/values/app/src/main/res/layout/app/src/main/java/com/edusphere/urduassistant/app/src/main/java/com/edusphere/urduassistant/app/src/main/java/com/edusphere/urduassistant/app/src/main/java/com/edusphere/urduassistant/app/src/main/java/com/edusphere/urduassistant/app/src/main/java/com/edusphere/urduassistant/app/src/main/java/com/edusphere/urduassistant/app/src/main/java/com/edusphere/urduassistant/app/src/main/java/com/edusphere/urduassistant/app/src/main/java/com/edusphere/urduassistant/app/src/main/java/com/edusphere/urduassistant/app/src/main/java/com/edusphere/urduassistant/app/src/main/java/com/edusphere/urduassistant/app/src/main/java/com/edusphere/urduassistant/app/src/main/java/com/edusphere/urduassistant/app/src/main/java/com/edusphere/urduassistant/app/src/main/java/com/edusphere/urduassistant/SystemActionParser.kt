package com.edusphere.urduassistant

object SystemActionParser {
    enum class Action { SETTINGS, CAMERA, FILES, TIME }

    fun parse(text: String): Action? {
        val t = text.trim().lowercase()
        return when {
            t in setOf(
                "time batao", "time bata do", "abhi time kya hai", "abhi kitne baje hain",
                "what time is it", "kitne baje hain", "time kya hai",
                "وقت بتاؤ", "وقت بتا دو", "ابھی وقت کیا ہے", "ابھی کتنے بجے ہیں", "وقت کیا ہے", "ٹائم کیا ہے"
            ) -> Action.TIME
            t in setOf("settings kholo", "settings khol do", "open settings", "سیٹنگز کھولو") -> Action.SETTINGS
            t in setOf("camera kholo", "camera khol do", "open camera", "کیمرہ کھولو") -> Action.CAMERA
            t in setOf("files kholo", "file manager kholo", "open files", "فائلیں کھولو") -> Action.FILES
            else -> null
        }
    }
}
