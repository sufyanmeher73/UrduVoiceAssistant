package com.edusphere.urduassistant

object MessageCommandParser {
    data class ParsedMessage(
        val personName: String,
        val message: String
    )

    fun parse(text: String): ParsedMessage? {
        val t = text.trim()
        val patterns = listOf(
            Regex("""(?i)^(?:sms|message|msg)\s+(?:to|ko)\s+(.+?)\s*[:,-]\s*(.+)$"""),
            Regex("""(?i)^(?:sms|message|msg)\s+(.+?)\s+(?:ko|k[oó])\s+(.+)$"""),
            Regex("""(?i)^(.+?)\s+ko\s+(?:sms|message|msg)\s+(?:karo|kro|bhejo)\s+(.+)$"""),
            Regex("""^(.+?)\s+کو\s+(?:ایس ایم ایس|میسج|پیغام)\s+(?:کرو|بھیجو)\s+(.+)$""")
        )
        for (p in patterns) {
            val m = p.find(t) ?: continue
            val person = m.groupValues[1].trim()
            val body = m.groupValues[2].trim()
            if (person.isNotBlank() && body.isNotBlank()) {
                return ParsedMessage(person, body)
            }
        }
        return null
    }
}
