package com.dragannovakovic.bblauncher.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

class AppCatalogRepository(context: Context) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val density = appContext.resources.displayMetrics.densityDpi
    private val iconSize = (64 * appContext.resources.displayMetrics.density).toInt()

    suspend fun loadApps(): List<LaunchableApp> = withContext(Dispatchers.IO) {
        val collator = Collator.getInstance()

        launcherApps
            .getActivityList(null, Process.myUserHandle())
            .asSequence()
            .filterNot { activity -> activity.componentName.packageName == appContext.packageName }
            .map(::toLaunchableApp)
            .sortedWith { first, second -> collator.compare(first.label, second.label) }
            .toList()
    }

    fun launch(app: LaunchableApp) {
        launcherApps.startMainActivity(
            app.componentName,
            app.user,
            null,
            null,
        )
    }

    fun registerPackageChangeCallback(onPackagesChanged: () -> Unit): LauncherApps.Callback {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: android.os.UserHandle) {
                onPackagesChanged()
            }

            override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) {
                onPackagesChanged()
            }

            override fun onPackageChanged(packageName: String, user: android.os.UserHandle) {
                onPackagesChanged()
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) {
                onPackagesChanged()
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) {
                onPackagesChanged()
            }

            override fun onPackagesSuspended(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
            ) {
                onPackagesChanged()
            }

            override fun onPackagesUnsuspended(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
            ) {
                onPackagesChanged()
            }
        }

        launcherApps.registerCallback(callback)
        return callback
    }

    fun unregisterPackageChangeCallback(callback: LauncherApps.Callback) {
        launcherApps.unregisterCallback(callback)
    }

    private fun toLaunchableApp(activity: LauncherActivityInfo): LaunchableApp {
        val componentName = activity.componentName
        return LaunchableApp(
            id = "${activity.user.hashCode()}:${componentName.flattenToString()}",
            label = activity.label.toString(),
            componentName = componentName,
            user = activity.user,
            icon = loadIcon(activity),
        )
    }

    private fun loadIcon(activity: LauncherActivityInfo): Bitmap {
        val drawable = try {
            activity.getBadgedIcon(density)
        } catch (_: Resources.NotFoundException) {
            appContext.packageManager.defaultActivityIcon
        }

        return drawable.toSquareBitmap(iconSize)
    }
}

private fun Drawable.toSquareBitmap(size: Int): Bitmap =
    toBitmap(
        width = size.coerceAtLeast(1),
        height = size.coerceAtLeast(1),
        config = Bitmap.Config.ARGB_8888,
    )
