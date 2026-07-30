package com.dragannovakovic.bblauncher.data.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.LauncherActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.UserManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

class AppCatalogRepository(context: Context) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)
    private val density = appContext.resources.displayMetrics.densityDpi
    private val iconSize = (64 * appContext.resources.displayMetrics.density).toInt()

    suspend fun loadApps(): List<LaunchableApp> = withContext(Dispatchers.IO) {
        val collator = Collator.getInstance()

        launcherApps
            .profiles
            .asSequence()
            .flatMap { user ->
                launcherApps.getActivityList(null, user).asSequence()
            }
            .filterNot { activity -> activity.componentName.packageName == appContext.packageName }
            .mapNotNull(::toLaunchableApp)
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

    fun registerPackageChangeCallback(
        onPackagesChanged: () -> Unit,
    ): PackageChangeRegistration {
        val launcherCallback = object : LauncherApps.Callback() {
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

        val profileReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                onPackagesChanged()
            }
        }
        val profileFilter = IntentFilter().apply {
            addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
            addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
        }

        launcherApps.registerCallback(launcherCallback)
        ContextCompat.registerReceiver(
            appContext,
            profileReceiver,
            profileFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        return PackageChangeRegistration(
            launcherCallback = launcherCallback,
            profileReceiver = profileReceiver,
        )
    }

    fun unregisterPackageChangeCallback(registration: PackageChangeRegistration) {
        launcherApps.unregisterCallback(registration.launcherCallback)
        appContext.unregisterReceiver(registration.profileReceiver)
    }

    private fun toLaunchableApp(activity: LauncherActivityInfo): LaunchableApp? {
        val componentName = activity.componentName
        val userSerial = userManager.getSerialNumberForUser(activity.user)
        if (userSerial < 0) {
            Log.e(
                LogTag,
                "Ignoring launcher activity for an unknown user: $componentName",
            )
            return null
        }
        return LaunchableApp(
            id = "$userSerial:${componentName.flattenToString()}",
            label = activity.label.toString(),
            componentName = componentName,
            user = activity.user,
            isWorkProfile = isManagedProfile(activity),
            icon = loadIcon(activity),
        )
    }

    private fun isManagedProfile(activity: LauncherActivityInfo): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return false
        }
        return launcherApps
            .getLauncherUserInfo(activity.user)
            ?.userType == UserManager.USER_TYPE_PROFILE_MANAGED
    }

    private fun loadIcon(activity: LauncherActivityInfo): Bitmap {
        val drawable = try {
            activity.getBadgedIcon(density)
        } catch (_: Resources.NotFoundException) {
            appContext.packageManager.defaultActivityIcon
        }

        return drawable.toSquareBitmap(iconSize)
    }

    companion object {
        private const val LogTag = "AppCatalogRepository"
    }
}

class PackageChangeRegistration internal constructor(
    internal val launcherCallback: LauncherApps.Callback,
    internal val profileReceiver: BroadcastReceiver,
)

private fun Drawable.toSquareBitmap(size: Int): Bitmap =
    toBitmap(
        width = size.coerceAtLeast(1),
        height = size.coerceAtLeast(1),
        config = Bitmap.Config.ARGB_8888,
    )
