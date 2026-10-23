package com.pleit.clearhome

import android.content.Intent
import android.content.pm.PackageManager

object AppRepository {

    /** Apps worth pinning on first run, in the order they should appear (only installed ones are used). */
    val defaultFavorites = listOf(
        "com.google.android.youtube.tv",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        "com.spotify.tv.android",
        "com.google.android.videos",
        "com.android.vending",
        "com.google.android.youtube"
    )

    fun load(pm: PackageManager, ownPackage: String): List<AppItem> {
        val seen = HashSet<String>()
        val result = ArrayList<AppItem>()

        // TV apps first, then regular launcher apps that don't declare a TV entry point.
        val categories = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
        for (category in categories) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            for (info in pm.queryIntentActivities(query, 0)) {
                val pkg = info.activityInfo.packageName
                if (pkg == ownPackage || !seen.add(pkg)) continue

                val launch = Intent(Intent.ACTION_MAIN)
                    .addCategory(category)
                    .setClassName(pkg, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                val banner = try {
                    info.activityInfo.loadBanner(pm)
                } catch (e: Exception) {
                    null
                }
                val image = banner ?: info.loadIcon(pm)

                result.add(
                    AppItem(
                        packageName = pkg,
                        label = info.loadLabel(pm).toString(),
                        image = image,
                        isBanner = banner != null,
                        launchIntent = launch
                    )
                )
            }
        }
        return result.sortedBy { it.label.lowercase() }
    }
}
