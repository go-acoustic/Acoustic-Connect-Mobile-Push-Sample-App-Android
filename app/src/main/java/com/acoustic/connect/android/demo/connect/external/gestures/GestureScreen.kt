/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external.gestures

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import kotlin.math.abs

/** Logical page name — kept identical to the XML sample app so signals are directly comparable. */
private const val SCREEN_NAME = "gestures_screen"

private const val LIST_ROWS = 40
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 4f

/** Travel a drag must clear before it counts as a swipe rather than a sloppy tap. */
private val SWIPE_THRESHOLD = 48.dp

/**
 * Gesture playground: one target per gesture category in CA-144239's scope, so each can be exercised
 * against a real control rather than empty screen space.
 *
 * <p>The gesture targets deliberately use plain Compose primitives rather than the SDK's `Logged*`
 * composables. Those wrappers emit their own events, which would mask whether the SDK's global
 * pointer handling reports the gesture — and that is exactly what this screen exists to observe. Only
 * the labels and list rows use `LoggedText`.
 *
 * <p>Screenview: `ConnectWrapper` logs the LOAD from the navigation route change, so this screen adds
 * only the matching UNLOAD on exit. The XML sample logs both explicitly; the emitted pair is the same.
 */
@Composable
fun GestureScreen() {
    var lastGesture by remember { mutableStateOf("none") }
    var zoom by remember { mutableFloatStateOf(1f) }

    ScreenviewUnloadEffect(SCREEN_NAME)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoggedText(
            text = "Gestures",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { testTag = "tv_gestures_title" },
        )

        LoggedText(
            text = "Last gesture: $lastGesture",
            modifier = Modifier.semantics { testTag = "tv_gestures_last" },
        )

        GestureTarget(
            label = "Long press me",
            testTagId = "gesture_long_press",
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { lastGesture = "tap" },
                    onLongPress = { lastGesture = "longPress" },
                )
            },
        )

        GestureTarget(
            label = "Double tap me",
            testTagId = "gesture_double_tap",
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { lastGesture = "doubleTap" })
            },
        )

        GestureTarget(
            label = "Swipe me — any direction",
            testTagId = "gesture_swipe",
            modifier = Modifier.pointerInput(Unit) {
                val thresholdPx = SWIPE_THRESHOLD.toPx()
                var travel = Offset.Zero
                detectDragGestures(
                    onDragStart = { travel = Offset.Zero },
                    // `detectDragGestures` consumes the pointer changes itself once touch slop
                    // is crossed, so a Compose-level observer would not see this drag. The SDK is
                    // expected to report it from the activity-level touch dispatch instead —
                    // confirming that is part of the CA-144239 audit.
                    onDrag = { _, dragAmount -> travel += dragAmount },
                    onDragEnd = { swipeName(travel, thresholdPx)?.let { lastGesture = it } },
                )
            },
        )

        Image(
            painter = painterResource(R.drawable.ic_launcher_logo),
            contentDescription = "Pinch to zoom",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .graphicsLayer(scaleX = zoom, scaleY = zoom)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, scaleChange, _ ->
                        zoom = (zoom * scaleChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        lastGesture = if (scaleChange > 1f) "zoomIn" else "zoomOut"
                    }
                }
                .semantics { testTag = "gesture_pinch_zoom" },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "gesture_scroll_list" },
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(LIST_ROWS) { index ->
                LoggedText(
                    text = "Scrollable row ${index + 1}",
                    modifier = Modifier.semantics { testTag = "gesture_row_$index" },
                )
            }
        }
    }
}

/**
 * Direction label for a completed drag, or null when neither axis cleared [SWIPE_THRESHOLD] — a
 * short drag that ends near where it began is not a swipe. The dominant axis wins, so a diagonal
 * still reports one direction.
 */
private fun swipeName(travel: Offset, thresholdPx: Float): String? = when {
    abs(travel.x) < thresholdPx && abs(travel.y) < thresholdPx -> null
    abs(travel.x) >= abs(travel.y) -> if (travel.x > 0) "swipeRight" else "swipeLeft"
    else -> if (travel.y > 0) "swipeDown" else "swipeUp"
}

/** Uniform tappable block — plain `Box`, so only the SDK's global gesture path can report it. */
@Composable
private fun GestureTarget(label: String, testTagId: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFFF0EEFF))
            .semantics { testTag = testTagId },
        contentAlignment = Alignment.Center,
    ) {
        LoggedText(text = label)
    }
}
