package com.example.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.data.models.InstalledAppInfo
import com.example.data.models.LauncherActionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "LauncherManager"

object LauncherManager {

    fun executeAction(context: Context, type: LauncherActionType, payload: String = ""): Boolean {
        return try {
            when (type) {
                LauncherActionType.PHONE -> openDialer(context, payload)
                LauncherActionType.CAMERA -> openCamera(context)
                LauncherActionType.MESSAGES -> openMessages(context)
                LauncherActionType.CONTACTS -> openContacts(context)
                LauncherActionType.SETTINGS -> openSettings(context)
                LauncherActionType.CLOCK -> openClock(context)
                LauncherActionType.CALCULATOR -> openCalculator(context)
                LauncherActionType.BROWSER -> openBrowser(context, if (payload.isNotBlank()) payload else "https://www.google.com")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute launcher action: $type", e)
            false
        }
    }

    fun openDialer(context: Context, number: String = "") {
        val intent = if (number.isNotBlank()) {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        } else {
            Intent(Intent.ACTION_DIAL)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openCamera(context: Context) {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openMessages(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MESSAGING)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Fallback if specific messaging category is not handled
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun openContacts(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openClock(context: Context) {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            openSettings(context)
        }
    }

    fun openCalculator(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Attempt to search calculator by common package
            val calcIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                ?: context.packageManager.getLaunchIntentForPackage("com.android.calculator2")
            if (calcIntent != null) {
                calcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(calcIntent)
            } else {
                openBrowser(context, "https://www.google.com/search?q=calculator")
            }
        }
    }

    fun openBrowser(context: Context, url: String) {
        val webUri = if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri.parse(url)
        } else {
            Uri.parse("https://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun launchAppByPackage(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching package: $packageName", e)
            false
        }
    }

    suspend fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                resolveInfos.mapNotNull { resolveInfo ->
                    val pkgName = resolveInfo.activityInfo.packageName
                    // Skip self from installed apps list for cleaner launcher display
                    if (pkgName == context.packageName) return@mapNotNull null

                    val label = resolveInfo.loadLabel(pm).toString()
                    val icon = resolveInfo.loadIcon(pm)
                    InstalledAppInfo(
                        label = label,
                        packageName = pkgName,
                        icon = icon
                    )
                }.sortedBy { it.label.lowercase() }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading installed apps", e)
                emptyList()
            }
        }
    }
}
