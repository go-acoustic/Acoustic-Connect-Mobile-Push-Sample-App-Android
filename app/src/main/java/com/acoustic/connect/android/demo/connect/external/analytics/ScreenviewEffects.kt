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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.acoustic.connect.android.connectmod.Connect
import com.tl.uic.model.ScreenviewType

/**
 * Logs a screenview UNLOAD for {@code screenName} when the composable leaves composition.
 *
 * <p>Only the UNLOAD half is logged here: `ConnectComposeUI.ConnectWrapper` already emits the LOAD
 * from the navigation route change, so logging it again would double-count. The XML sample app logs
 * both halves explicitly because it has no equivalent automatic route observer — the emitted signal
 * pair is identical either way, which is what keeps the two apps comparable.
 */
@Composable
fun ScreenviewUnloadEffect(screenName: String) {
    val context = LocalContext.current
    DisposableEffect(screenName) {
        onDispose {
            (context as? Activity)?.let { activity ->
                Connect.logScreenview(activity, screenName, ScreenviewType.UNLOAD)
            }
        }
    }
}
