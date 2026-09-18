package com.edusphere.urduassistant

/** Parses app-specific search commands without pretending to automate app internals. */
object AppSearchCommandParser {
    data class Parsed(val app: String, val query: String)

    private val aliases = mapOf(
        "youtube" to setOf("youtube", "you tube", "یوٹیوب", "یو ٹیوب"),
        "tiktok" to setOf("tiktok", "tik tok", "ٹک ٹاک", "ٹک ٹوک"),
        "instagram" to setOf("instagram", "insta", "انسٹاگرام"),
        "facebook" to setOf("facebook", "fb", "فیس بک"),
        "google" to setOf("google", "گوگل"),
        "maps" to setOf("maps", "google maps", "map", "گوگل میپس", "میپس")
    )

    private val searchWords = listOf(
        "search", "search karo", "search kro", "find", "find karo", "find kro",
        "dhoondo", "dhundo", "تلاش", "تلاش کرو", "ڈھونڈو", "سرچ کرو", "سرچ"
    )

    fun parse(text: String): Parsed? {
        val t = text.trim().lowercase()
        for ((app, names) in aliases) {
            for (name in names) {
                val escaped = Regex.escape(name)
                val patterns = listOf(
                    Regex("""^$escaped\s+(?:par|pe|mein|me|پر|پہ|میں)\s+(.+?)\s+(?:search|find|dhoondo|dhundo|تلاش|ڈھونڈو|سرچ)(?:\s+(?:karo|kro|کریں|کرو))?$"""),
                    Regex("""^$escaped\s+(?:search|find|تلاش|سرچ)\s+(.+)$"""),
                )
                for (pattern in patterns) {
                    val match = pattern.find(t) ?: continue
                    val query = match.groupValues[1].trim()
                    if (query.isNotBlank()) return Parsed(app, query)
                }
            }
        }
        return null
    }
}
