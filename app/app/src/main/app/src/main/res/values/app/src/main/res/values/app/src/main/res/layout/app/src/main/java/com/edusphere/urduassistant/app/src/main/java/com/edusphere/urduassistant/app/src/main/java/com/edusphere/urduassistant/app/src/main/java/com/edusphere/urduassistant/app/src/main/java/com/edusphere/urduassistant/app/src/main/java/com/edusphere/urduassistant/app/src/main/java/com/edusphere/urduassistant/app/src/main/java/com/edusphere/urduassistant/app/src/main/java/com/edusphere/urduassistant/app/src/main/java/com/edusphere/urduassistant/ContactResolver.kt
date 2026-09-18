package com.edusphere.urduassistant

import android.content.Context
import android.provider.ContactsContract

object ContactResolver {

    data class ContactMatch(
        val displayName: String,
        val phoneNumber: String,
        val score: Int
    )

    /** Returns a contact only when the best match is sufficiently confident.
     *  If two strong candidates are too close, refuse to guess so callers can
     *  ask the user to clarify instead of calling/messaging the wrong person.
     */
    fun resolvePhoneNumber(context: Context, personName: String): String? =
        resolveBest(context, personName)?.phoneNumber

    fun resolveBest(context: Context, personName: String): ContactMatch? {
        val target = normalize(personName)
        if (target.isBlank()) return null

        val matches = findMatches(context, target)
        val best = matches.firstOrNull() ?: return null
        if (best.score < 65) return null

        val second = matches.getOrNull(1)
        if (second != null && second.score >= 65 && (best.score - second.score) < 12) {
            return null
        }
        return best
    }

    fun findMatches(context: Context, personName: String): List<ContactMatch> {
        val target = normalize(personName)
        if (target.isBlank()) return emptyList()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val results = mutableListOf<ContactMatch>()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val numberIndex = cursor.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            while (cursor.moveToNext()) {
                if (nameIndex < 0 || numberIndex < 0) continue
                val name = cursor.getString(nameIndex) ?: continue
                val number = cursor.getString(numberIndex) ?: continue
                val score = matchScore(target, normalize(name))
                if (score >= 50) {
                    results += ContactMatch(name, number, score)
                }
            }
        }

        return results
            .sortedByDescending { it.score }
            .distinctBy { it.phoneNumber }
            .take(5)
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchScore(target: String, candidate: String): Int {
        if (target == candidate) return 100

        val t = target.split(" ").filter { it.isNotBlank() }
        val c = candidate.split(" ").filter { it.isNotBlank() }
        if (t.isEmpty() || c.isEmpty()) return 0

        val common = t.count { token ->
            c.any { it == token }
        }
        val partial = t.count { token ->
            c.any { it.startsWith(token) || token.startsWith(it) }
        }

        val tokenScore = (common * 80 / t.size).coerceAtMost(80)
        val partialScore = (partial * 65 / t.size).coerceAtMost(65)

        // Strong single-token partial match, useful for short names.
        val containsScore = if (t.size == 1 && c.any { it.contains(t[0]) }) 72 else 0

        return maxOf(tokenScore, partialScore, containsScore)
    }
}
