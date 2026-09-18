package com.edusphere.urduassistant

object CallCommandParser {
    data class Parsed(val personName: String? = null, val phoneNumber: String? = null)

    fun parse(text: String): Parsed? {
        val t = text.trim()
        val number = Regex("""(\+?\d[\d\s-]{7,}\d)""").find(t)?.value?.replace(" ", "")?.replace("-", "")
        if (number != null && Regex("(?i)\\b(call|phone)\\b|کال|فون").containsMatchIn(t)) return Parsed(phoneNumber = number)

        val patterns = listOf(
            Regex("""(?i)^(?:call|phone)\s+(.+?)(?:\s+(?:ko|karo|kro|kar do))?$"""),
            Regex("""(?i)^(.+?)\s+(?:ko)\s+(?:call|phone)\s*(?:karo|kro|kar do)?$"""),
            Regex("""^(.+?)\s+(?:کو)\s+(?:کال|فون)\s*(?:کرو|کر دو)?$""")
        )
        for (p in patterns) {
            val m = p.find(t) ?: continue
            val person = m.groupValues[1].trim()
            if (person.isNotBlank()) return Parsed(personName = person)
        }
        return null
    }
}
