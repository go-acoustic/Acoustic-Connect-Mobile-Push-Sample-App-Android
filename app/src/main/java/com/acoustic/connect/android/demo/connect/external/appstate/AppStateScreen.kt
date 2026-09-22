/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external.appstate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedButton
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.analytics.SignalLog
import com.acoustic.connect.android.demo.connect.external.analytics.currentLogicalPageName
import com.acoustic.connect.android.demo.connect.external.analytics.currentSessionId
import com.acoustic.connect.android.demo.connect.external.ui.theme.LightCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Logical page name — kept identical to the XML sample app so signals are directly comparable. */
private const val SCREEN_NAME = "app_state_screen"

private const val EVENT_SESSION_START = "sessionStart"

/**
 * App-state readout for the analytics audit.
 *
 * <p>Shows the SDK state that the app-state signals are meant to carry — session id, logical page,
 * enabled flag — next to the running [SignalLog], so a foreground, background or rotation can be
 * checked on the device rather than only in a collector payload.
 *
 * <p>Session id is read on resume rather than observed: the SDK exposes no change notification for
 * it, so a session that rolls over while this screen is showing will not update until the screen is
 * revisited.
 */
@Composable
fun AppStateScreen(modifier: Modifier = Modifier) {
    var sdkEnabled by remember { mutableStateOf(false) }
    var sessionId by remember { mutableStateOf("") }
    var logicalPage by remember { mutableStateOf("") }

    val entries by SignalLog.entries.collectAsStateWithLifecycle()

    // Read unconditionally rather than behind isEnabled(): that flag reads false in this
    // integration even while the SDK is running, and hiding the session id behind it made the
    // screen look broken when it was the flag that was wrong.
    fun refresh() {
        sdkEnabled = Connect.isEnabled()
        sessionId = currentSessionId()
        logicalPage = currentLogicalPageName()
    }

    ScreenviewUnloadEffect(SCREEN_NAME)

    LifecycleResumeEffect(Unit) {
        refresh()
        onPauseOrDispose { }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoggedText(
            text = "App state",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { testTag = "tv_app_state_title" },
        )

        StateRow(label = "SDK enabled", value = sdkEnabled.toString(), testTagId = "tv_sdk_enabled")
        StateRow(
            label = "Session id",
            value = sessionId.ifEmpty { "—" },
            testTagId = "tv_session_id",
        )
        StateRow(
            label = "Logical page",
            value = logicalPage.ifEmpty { "—" },
            testTagId = "tv_logical_page",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoggedButton(
                onClick = {
                    val started = Connect.startSession()
                    SignalLog.record(EVENT_SESSION_START, "Connect.startSession()", started)
                    refresh()
                },
                modifier = Modifier.semantics { testTag = "btn_start_session" },
            ) {
                LoggedText(text = "Start session")
            }

            LoggedButton(
                onClick = { SignalLog.clear() },
                modifier = Modifier.semantics { testTag = "btn_clear_signals" },
            ) {
                LoggedText(text = "Clear log")
            }
        }

        LoggedText(
            text = "Signals emitted by this app (${entries.size})",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { testTag = "tv_signal_log_header" },
        )

        if (entries.isEmpty()) {
            LoggedText(
                text = "None yet. Background the app, rotate it, or start a session.",
                modifier = Modifier.semantics { testTag = "tv_signal_log_empty" },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "list_signal_log" },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(entries) { entry ->
                    SignalRow(entry)
                }
            }
        }
    }
}

@Composable
private fun StateRow(label: String, value: String, testTagId: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LoggedText(text = label)
        LoggedText(
            text = value,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.semantics { testTag = testTagId },
        )
    }
}

@Composable
private fun SignalRow(entry: SignalLog.Entry) {
    Surface(color = LightCard, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            LoggedText(
                text = "${formatTime(entry.timestampMillis)}  ${entry.name}  ${outcome(entry.accepted)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            )
            if (entry.detail.isNotEmpty()) {
                LoggedText(text = entry.detail, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

private fun outcome(accepted: Boolean?): String = when (accepted) {
    true -> "accepted"
    false -> "rejected"
    null -> "not sent"
}

private fun formatTime(timestampMillis: Long): String =
    SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestampMillis))
