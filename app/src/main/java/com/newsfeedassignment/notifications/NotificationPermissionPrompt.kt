package com.newsfeedassignment.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionPrompt() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(PREFERENCES_NAME, 0) }
    var visible by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !preferences.getBoolean(PROMPT_SHOWN_KEY, false),
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        preferences.edit().putBoolean(PROMPT_SHOWN_KEY, true).apply()
        visible = false
    }
    if (!visible) return
    AlertDialog(
        onDismissRequest = {
            preferences.edit().putBoolean(PROMPT_SHOWN_KEY, true).apply()
            visible = false
        },
        title = { Text("Stay up to date") },
        text = { Text("Allow notifications when the background sync finds new articles?") },
        confirmButton = {
            Button(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                preferences.edit().putBoolean(PROMPT_SHOWN_KEY, true).apply()
                visible = false
            }) { Text("Not now") }
        },
    )
}

private const val PREFERENCES_NAME = "notification_preferences"
private const val PROMPT_SHOWN_KEY = "notification_prompt_shown"
