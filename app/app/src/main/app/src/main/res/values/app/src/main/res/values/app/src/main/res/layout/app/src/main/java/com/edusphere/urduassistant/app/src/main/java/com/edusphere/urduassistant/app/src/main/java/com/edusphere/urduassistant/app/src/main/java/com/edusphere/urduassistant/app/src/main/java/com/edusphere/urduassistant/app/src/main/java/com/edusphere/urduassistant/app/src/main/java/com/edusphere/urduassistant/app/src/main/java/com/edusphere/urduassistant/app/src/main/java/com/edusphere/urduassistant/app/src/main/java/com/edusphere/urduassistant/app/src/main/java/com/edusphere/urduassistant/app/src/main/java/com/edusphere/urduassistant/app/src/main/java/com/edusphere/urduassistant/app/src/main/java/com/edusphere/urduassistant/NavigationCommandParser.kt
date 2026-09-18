package com.edusphere.urduassistant

object NavigationCommandParser {
    fun parse(text: String): String? {
        val t = text.trim()
        val patterns = listOf(
            Regex("""(?i)^(?:maps?|google maps)\s+(?:mein|me|par|pe)\s+(.+?)\s+(?:jana hai|jao|jaana|navigate|navigation|le chalo|dhoondo|dhundo)$"""),
            Regex("""(?i)^(?:mujhe|mujhko)\s+(.+?)\s+(?:le chalo|ka rasta batao|ka raasta batao)$"""),
            Regex("""(?i)^(?:rasta|raasta|navigation)\s+(?:to|for|ka|ki)\s+(.+)$"""),
            Regex("""^(.+?)\s+(?:کا|کی)\s+(?:راستہ|نیویگیشن)\s+(?:کھولو|بتاؤ)$""")
        )
        return patterns.firstNotNullOfOrNull { p ->
            p.find(t)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
