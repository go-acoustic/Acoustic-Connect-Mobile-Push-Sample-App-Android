/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external.analytics

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import com.acoustic.connect.android.connectmod.Connect

/** Signal names — kept identical to the XML sample app so the two payloads line up. */
const val EVENT_APP_FOREGROUND = "appForeground"
const val EVENT_APP_BACKGROUND = "appBackground"
const val EVENT_ORIENTATION_CHANGE = "orientationChange"

/**
 * Process-level app-state instrumentation for the CA-144239 signal audit.
 *
 * <p>The SDK already covers part of the app-state surface through `ConnectComposeUI.ConnectWrapper`:
 * its `ComposeUiLifecycle` observer calls `Connect.onResume`/`Connect.onPause` on the composition's
 * lifecycle, and its navigation listener emits the screenview LOAD on each route change. Those are
 * deliberately not repeated here — duplicating them is exactly the kind of double-count the ticket
 * is looking for.
 *
 * <p>What the SDK does not derive on its own is *process* state. `Connect.onPause` fires whenever
 * the activity pauses, which is not the same as the app leaving the foreground, and nothing reports
 * an orientation change at all. Both are emitted here as custom events, so they are unambiguously
 * attributable to the app rather than to SDK auto-instrumentation when the payloads are compared.
 *
 * <p>Deliberately not called: `Connect.onPauseNoActivityInForeground()`. It is the SDK-native way to
 * say the process left the foreground, but the Compose lifecycle handler has already emitted an
 * `onPause` for the activity by that point, so whether the pair double-counts is one of the
 * questions the audit has to answer on real payloads first.
 */
object AppStateSignals {

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(ForegroundTracker())
        application.registerComponentCallbacks(OrientationTracker(application.resources.configuration.orientation))
    }

    /**
     * Emits [name] as a custom event, tagged with the session it belongs to so the collector-side
     * payload can be matched back to a specific run.
     *
     * <p>Calls made before the SDK is enabled are recorded but not sent: `ConnectWrapper` enables
     * the SDK from a `LaunchedEffect`, so the first activity start of a cold launch can land first.
     */
    internal fun emit(name: String, extras: Map<String, String> = emptyMap()) {
        if (!Connect.isEnabled()) {
            SignalLog.record(name, "not sent — SDK not enabled yet", accepted = null)
            return
        }
        val sessionId = Connect.getCurrentSessionId().orEmpty()
        val payload = extras + ("sessionId" to sessionId)
        // The SDK declares the payload as HashMap<String?, String?>, so the nullable element types
        // have to be spelled out even though nothing here puts a null in it.
        val accepted = Connect.logCustomEvent(name, HashMap<String?, String?>(payload))
        SignalLog.record(name, describe(payload), accepted)
    }

    private fun describe(payload: Map<String, String>): String =
        payload.entries.joinToString(", ") { "${it.key}=${it.value}" }
}

/**
 * Foreground/background from the count of started activities.
 *
 * <p>A rotation stops and restarts the activity, which would otherwise read as a background trip
 * followed by a return. [Activity.isChangingConfigurations] identifies that stop, and the flag
 * carries the knowledge across to the matching start so neither half is reported.
 */
private class ForegroundTracker : Application.ActivityLifecycleCallbacks {

    private var startedActivities = 0
    private var restartingForConfigChange = false

    override fun onActivityStarted(activity: Activity) {
        val enteringForeground = startedActivities == 0 && !restartingForConfigChange
        startedActivities++
        restartingForConfigChange = false
        if (enteringForeground) {
            AppStateSignals.emit(EVENT_APP_FOREGROUND)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities--
        restartingForConfigChange = activity.isChangingConfigurations
        if (startedActivities == 0 && !restartingForConfigChange) {
            AppStateSignals.emit(EVENT_APP_BACKGROUND)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

/**
 * Orientation changes, reported once per actual rotation.
 *
 * <p>`onConfigurationChanged` fires for any configuration delta — locale, font scale, dark mode —
 * so the previous orientation is held to filter out the changes that are not rotations.
 */
private class OrientationTracker(initialOrientation: Int) : ComponentCallbacks {

    private var lastOrientation = initialOrientation

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == lastOrientation) return
        val from = orientationName(lastOrientation)
        lastOrientation = newConfig.orientation
        AppStateSignals.emit(
            EVENT_ORIENTATION_CHANGE,
            mapOf("from" to from, "to" to orientationName(newConfig.orientation)),
        )
    }

    override fun onLowMemory() = Unit

    private fun orientationName(orientation: Int): String = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> "landscape"
        Configuration.ORIENTATION_PORTRAIT -> "portrait"
        else -> "undefined"
    }
}
