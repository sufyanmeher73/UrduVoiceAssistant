package com.edusphere.urduassistant

object AppOpenCommandParser {

    private val aliases = mapOf(
        "whatsapp" to setOf("whatsapp", "watsapp", "واٹس ایپ"),
        "youtube" to setOf("youtube", "you tube", "یوٹیوب"),
        "tiktok" to setOf("tiktok", "tik tok", "ٹک ٹاک"),
        "instagram" to setOf("instagram", "insta", "انسٹاگرام"),
        "facebook" to setOf("facebook", "fb", "فیس بک"),
        "chrome" to setOf("chrome", "کروم"),
        "calculator" to setOf("calculator", "calc", "کیلکولیٹر"),
        "maps" to setOf("maps", "google maps", "map", "گوگل میپس", "میپس")
    )

    fun parse(text: String): String? {
        val t = text.trim().lowercase()
        for ((canonical, names) in aliases) {
            if (names.any { n ->
                    t == n || t == "open $n" || t == "launch $n" ||
                    t == "$n kholo" || t == "$n khol do" ||
                    t == "$n کھولو" || t == "$n کھول دو"
                }) return canonical
        }

        // Generic installed-app command: "flan app kholo" / "open flan app".
        val generic = Regex("^(?:open|launch)\\s+(.+)$", RegexOption.IGNORE_CASE).matchEntire(t)
            ?: Regex("^(.+?)\\s+(?:kholo|khol do|کھولو|کھول دو)$", RegexOption.IGNORE_CASE).matchEntire(t)
        return generic?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }
}
