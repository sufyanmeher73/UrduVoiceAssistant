package com.edusphere.urduassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object CallRouter {
    fun execute(context: Context, parsed: CallCommandParser.Parsed): CommandExecutionCoordinator.Result {
        val number = parsed.phoneNumber ?: run {
            val person = parsed.personName ?: return CommandExecutionCoordinator.Result(false, "Kis ko call karni hai?")
            if (!ContactPermissionGate.ensure(context)) {
                return CommandExecutionCoordinator.Result(false, "Contacts ki permission darkar hai. Permission dene ke baad command dobara dein.")
            }
            ContactResolver.resolveBest(context, person)?.phoneNumber
                ?: return CommandExecutionCoordinator.Result(true, "Contact nahi mila.")
        }

        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            (context as? Activity)?.requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 11)
            return CommandExecutionCoordinator.Result(false, "Call ki permission darkar hai. Permission dene ke baad command dobara dein.")
        }

        return try {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
            CommandExecutionCoordinator.Result(true, "Call kar raha hoon.")
        } catch (_: Exception) {
            CommandExecutionCoordinator.Result(false, "Call start nahi ho saki.")
        }
    }
}
