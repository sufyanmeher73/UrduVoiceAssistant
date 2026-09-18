package com.edusphere.urduassistant

import android.content.Context
import android.content.Intent
import android.net.Uri

object AppActionRouter {

    data class RouteResult(val handled: Boolean, val message: String)

    fun search(context: Context, target: String, query: String): RouteResult {
        val q = query.trim()
        if (q.isBlank()) return RouteResult(false, "Search kya karna hai?")

        val key = target.lowercase()
        val (url, packageName) = when (key) {
            "youtube" -> "https://www.youtube.com/results?search_query=${Uri.encode(q)}" to "com.google.android.youtube"
            "tiktok" -> "https://www.tiktok.com/search?q=${Uri.encode(q)}" to "com.zhiliaoapp.musically"
            "instagram" -> "https://www.instagram.com/explore/search/keyword/?q=${Uri.encode(q)}" to "com.instagram.android"
            "facebook" -> "https://www.facebook.com/search/top?q=${Uri.encode(q)}" to "com.facebook.katana"
            "google" -> "https://www.google.com/search?q=${Uri.encode(q)}" to null
            "maps" -> "https://www.google.com/maps/search/?api=1&query=${Uri.encode(q)}" to "com.google.android.apps.maps"
            else -> "https://www.google.com/search?q=${Uri.encode(q)}" to null
        }
        return openUri(context, Uri.parse(url), packageName, "$target search")
    }

    fun navigate(context: Context, destination: String): RouteResult {
        val place = destination.trim()
        if (place.isBlank()) return RouteResult(false, "Kahan jana hai?")
        val geo = Uri.parse("google.navigation:q=${Uri.encode(place)}")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, geo).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            return RouteResult(true, "Google Maps mein navigation khol raha hoon.")
        } catch (_: Exception) {
            val web = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(place)}"
            )
            return openUri(context, web, null, "Maps navigation")
        }
    }

    fun openMapPlace(context: Context, place: String): RouteResult =
        search(context, "maps", place)

    private fun openUri(
        context: Context,
        uri: Uri,
        packageName: String?,
        label: String
    ): RouteResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (!packageName.isNullOrBlank()) setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            RouteResult(true, "$label khol raha hoon.")
        } catch (_: Exception) {
            if (!packageName.isNullOrBlank()) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    return RouteResult(true, "$label web par khol raha hoon.")
                } catch (_: Exception) {}
            }
            RouteResult(false, "$label open nahi ho saka.")
        }
    }
}
