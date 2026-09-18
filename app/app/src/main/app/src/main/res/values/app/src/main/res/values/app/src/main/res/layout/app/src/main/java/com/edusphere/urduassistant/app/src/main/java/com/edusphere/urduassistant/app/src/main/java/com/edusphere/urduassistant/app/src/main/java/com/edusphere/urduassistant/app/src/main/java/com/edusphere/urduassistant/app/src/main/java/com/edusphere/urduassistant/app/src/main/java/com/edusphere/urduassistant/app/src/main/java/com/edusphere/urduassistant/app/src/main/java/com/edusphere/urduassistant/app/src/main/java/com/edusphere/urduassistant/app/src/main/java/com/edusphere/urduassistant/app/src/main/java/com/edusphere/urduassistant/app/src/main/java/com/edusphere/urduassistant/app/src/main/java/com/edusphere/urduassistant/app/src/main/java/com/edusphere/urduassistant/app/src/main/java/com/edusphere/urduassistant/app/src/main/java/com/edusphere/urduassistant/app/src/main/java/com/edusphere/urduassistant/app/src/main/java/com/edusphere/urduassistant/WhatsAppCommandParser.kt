package com.edusphere.urduassistant

object WhatsAppCommandParser {
    data class Parsed(val personName: String?, val message: String)

    fun parse(text: String): Parsed? {
        val t = text.trim()
        val patterns = listOf(
            Regex("""(?i)^(?:whatsapp|watsapp)\s+(?:par|pe|mein)\s+(.+?)\s+(?:ko|kay)\s+(?:message|msg)\s+(?:karo|kro|bhejo)\s+(.+)$"""),
            Regex("""(?i)^(.+?)\s+(?:ko)\s+whatsapp\s+(?:par|pe)\s+(?:message|msg)\s+(?:karo|kro|bhejo)\s+(.+)$"""),
            Regex("""(?i)^(?:whatsapp|watsapp)\s+(.+?)\s*[:,-]\s*(.+)$"""),
            Regex("""^(.+?)\s+کو\s+واٹس ایپ\s+(?:پر|پہ)\s+(?:میسج|پیغام)\s+(?:کرو|بھیجو)\s+(.+)$""")
        )
        for ((i, p) in patterns.withIndex()) {
            val m = p.find(t) ?: continue
            return if (i == 2) Parsed(null, m.groupValues[2].trim())
            else Parsed(m.groupValues[1].trim(), m.groupValues[2].trim())
        }
        return null
    }
}
