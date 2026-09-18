package com.edusphere.urduassistant

object MapSearchCommandParser {
    fun parse(text: String): String? {
        val t = text.trim()
        val patterns = listOf(
            Regex("""(?i)^(?:maps?|google maps)\s+(?:mein|me|par|pe)\s+(.+?)\s+(?:dhoondo|dhundo|search karo|search kro|find karo|find kro)$"""),
            Regex("""(?i)^(.+?)\s+(?:near me|mere paas|mere qareeb|qareeb)\s+(?:dhoondo|dhundo|search karo|find karo)$"""),
            Regex("""^(.+?)\s+(?:قریب|قریبی)\s+(?:تلاش کرو|ڈھونڈو)$""")
        )
        return patterns.firstNotNullOfOrNull { p ->
            p.find(t)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
