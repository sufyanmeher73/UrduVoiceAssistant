package com.edusphere.urduassistant

object MultiActionCommandEngine {

    data class Action(val raw: String, val type: Type)
    enum class Type { OPEN_APP, SEARCH, CALL, SMS, WHATSAPP, NAVIGATE, TIME, SYSTEM, UNKNOWN }

    /**
     * Splits a spoken command without blindly treating every "aur/and/phir"
     * inside a message or search query as a new action boundary.
     */
    fun split(text: String): List<String> {
        val t = text.trim()
        if (t.isBlank()) return emptyList()

        val pieces = t.split(Regex("\\s+(?:aur|and|phir|then)\\s+|[،؛;]\\s*"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (pieces.size <= 1) return listOf(t)

        val merged = mutableListOf<String>()
        for (piece in pieces) {
            if (merged.isEmpty()) {
                merged += piece
                continue
            }

            val previous = merged.last()
            val previousLooksLikeMessage = MessageCommandParser.parse(previous) != null ||
                WhatsAppCommandParser.parse(previous) != null
            val currentLooksLikeAction = looksLikeActionStart(piece)

            // A separator inside message text is data, not a new command.
            if (previousLooksLikeMessage && !currentLooksLikeAction) {
                merged[merged.lastIndex] = "$previous aur $piece"
            } else {
                merged += piece
            }
        }
        return merged
    }

    private fun looksLikeActionStart(segment: String): Boolean {
        val s = segment.trim().lowercase()
        return AppOpenCommandParser.parse(segment) != null ||
            AppSearchCommandParser.parse(segment) != null ||
            NavigationCommandParser.parse(segment) != null ||
            MapSearchCommandParser.parse(segment) != null ||
            CallCommandParser.parse(segment) != null ||
            MessageCommandParser.parse(segment) != null ||
            WhatsAppCommandParser.parse(segment) != null ||
            SystemActionParser.parse(segment) != null ||
            s.startsWith("search ") || s.startsWith("سرچ ") ||
            s.startsWith("open ") || s.startsWith("کھولو ") ||
            s.startsWith("call ") || s.startsWith("کال ") ||
            s.startsWith("sms ") || s.startsWith("message ") ||
            s.startsWith("whatsapp ") || s.startsWith("maps ")
    }

    fun classify(segment: String): Action {
        val s = segment.lowercase().trim()
        val type = when {
            s.contains("whatsapp") || s.contains("watsapp") || s.contains("واٹس ایپ") -> Type.WHATSAPP
            s.contains(" sms ") || s.startsWith("sms ") || s.contains("message") || s.contains("msg ") -> Type.SMS
            CallCommandParser.parse(segment) != null -> Type.CALL
            s.contains("maps") || s.contains("navigation") || s.contains("rasta") || s.contains("راستہ") -> Type.NAVIGATE
            AppSearchCommandParser.parse(segment) != null || s.contains("search") || s.contains("سرچ") -> Type.SEARCH
            SystemActionParser.parse(segment) != null -> Type.SYSTEM
            s == "time batao" || s.contains("what time") || s.contains("kitne baje") ||
                s.contains("وقت") -> Type.TIME
            AppOpenCommandParser.parse(segment) != null -> Type.OPEN_APP
            else -> Type.UNKNOWN
        }
        return Action(segment, type)
    }

    fun plan(text: String): List<Action> = split(text).map(::classify)

    /** Executes planned actions in order and stops at the first unhandled action. */
    fun execute(context: android.content.Context, text: String, executor: (android.content.Context, String) -> CommandExecutionCoordinator.Result = CommandExecutionCoordinator::executeOne): CommandExecutionCoordinator.Result {
        val actions = plan(text)
        if (actions.isEmpty()) return CommandExecutionCoordinator.Result(false, "Command samajh nahi aayi.")
        val completed = mutableListOf<String>()
        for ((index, action) in actions.withIndex()) {
            if (action.type == Type.UNKNOWN) {
                return CommandExecutionCoordinator.Result(false, "Command #${index + 1} samajh nahi aayi. Baqi actions stop kar di gayi hain.")
            }
            val result = executor(context, action.raw)
            if (!result.handled) {
                val remaining = actions.size - index - 1
                val suffix = if (remaining > 0) " $remaining aur action stop kar di gayi hain." else ""
                return CommandExecutionCoordinator.Result(false, "Action #${index + 1} complete nahi hui.${suffix}")
            }
            completed += action.raw
        }
        return CommandExecutionCoordinator.Result(true, "${completed.size} action complete.")
    }

}
