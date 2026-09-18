package com.edusphere.urduassistant

import android.content.Context

object CommandExecutionCoordinator {

    data class Result(
        val handled: Boolean,
        val message: String
    )

    fun executeOne(context: Context, segment: String): Result {
        if (segment.isBlank()) return Result(false, "Command khali hai.")
        return runCatching { executeOneInternal(context, segment) }
            .getOrElse { Result(false, "Command execute nahi ho saki.") }
    }

    private fun executeOneInternal(context: Context, segment: String): Result {
        val system = SystemActionParser.parse(segment)
        if (system != null) {
            val action = when (system) {
                SystemActionParser.Action.SETTINGS -> SystemActionRouter.Action.SETTINGS
                SystemActionParser.Action.CAMERA -> SystemActionRouter.Action.CAMERA
                SystemActionParser.Action.FILES -> SystemActionRouter.Action.FILES
                SystemActionParser.Action.TIME -> SystemActionRouter.Action.TIME
            }
            val result = SystemActionRouter.execute(context, action)
            return Result(result.handled, result.message)
        }

        val appSearch = AppSearchCommandParser.parse(segment)
        if (appSearch != null) {
            val route = AppActionRouter.search(context, appSearch.app, appSearch.query)
            return Result(route.handled, route.message)
        }

        val app = AppOpenCommandParser.parse(segment)
        if (app != null) return CommonAppActions.open(context, app)

        val nav = NavigationCommandParser.parse(segment)
        if (nav != null) return AppActionRouter.navigate(context, nav)

        val mapSearch = MapSearchCommandParser.parse(segment)
        if (mapSearch != null) return AppActionRouter.openMapPlace(context, mapSearch)

        val call = CallCommandParser.parse(segment)
        if (call != null) return CallRouter.execute(context, call)

        val message = MessageCommandParser.parse(segment)
        if (message != null) {
            if (!ContactPermissionGate.ensure(context)) {
                return Result(false, "Contacts ki permission darkar hai. Permission dene ke baad command dobara dein.")
            }
            val matches = ContactResolver.findMatches(context, message.personName)
            val phone = ContactResolver.resolvePhoneNumber(context, message.personName)
            return when {
                phone != null -> SmsRouter.composeToNumber(context, phone, message.message)
                matches.size > 1 && matches.first().score >= 65 ->
                    Result(false, "Ek se zyada matching contacts mile. Naam ya number thora clear bata dein.")
                else -> Result(false, "Contact nahi mila.")
            }
        }

        val whatsapp = WhatsAppCommandParser.parse(segment)
        if (whatsapp != null) {
            if (whatsapp.personName != null && !ContactPermissionGate.ensure(context)) {
                return Result(false, "Contacts ki permission darkar hai. Permission dene ke baad command dobara dein.")
            }
            val matches = whatsapp.personName?.let { ContactResolver.findMatches(context, it) }.orEmpty()
            val phone = whatsapp.personName?.let { ContactResolver.resolvePhoneNumber(context, it) }
            return when {
                whatsapp.personName != null && phone == null && matches.size > 1 && matches.first().score >= 65 ->
                    Result(false, "Ek se zyada matching WhatsApp contacts mile. Naam ya number thora clear bata dein.")
                whatsapp.personName != null && phone == null ->
                    Result(false, "WhatsApp contact nahi mila.")
                else -> WhatsAppRouter.compose(context, phone, whatsapp.message)
            }
        }

        return Result(false, "Command samajh nahi aayi.")
    }
}
