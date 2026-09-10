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

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val THRESHOLD = 100f

class SwipeNameTest {

    @Test
    fun `reports no swipe when neither axis clears the threshold`() {
        assertNull(swipeName(Offset(99f, 99f), THRESHOLD))
    }

    @Test
    fun `reports no swipe for a drag that returns to its origin`() {
        assertNull(swipeName(Offset.Zero, THRESHOLD))
    }

    @Test
    fun `reports horizontal direction from the sign of the travel`() {
        assertEquals("swipeRight", swipeName(Offset(150f, 0f), THRESHOLD))
        assertEquals("swipeLeft", swipeName(Offset(-150f, 0f), THRESHOLD))
    }

    @Test
    fun `reports vertical direction from the sign of the travel`() {
        // Compose y grows downwards, so positive travel is a downward swipe.
        assertEquals("swipeDown", swipeName(Offset(0f, 150f), THRESHOLD))
        assertEquals("swipeUp", swipeName(Offset(0f, -150f), THRESHOLD))
    }

    @Test
    fun `resolves a diagonal to its dominant axis`() {
        assertEquals("swipeRight", swipeName(Offset(200f, 120f), THRESHOLD))
        assertEquals("swipeDown", swipeName(Offset(120f, 200f), THRESHOLD))
    }

    @Test
    fun `treats a perfect diagonal as horizontal`() {
        assertEquals("swipeRight", swipeName(Offset(150f, 150f), THRESHOLD))
    }

    @Test
    fun `reports a swipe when only one axis clears the threshold`() {
        // Mostly sideways with a little drift: the drift must not suppress the swipe.
        assertEquals("swipeRight", swipeName(Offset(150f, 10f), THRESHOLD))
    }

    @Test
    fun `treats travel exactly at the threshold as a swipe`() {
        assertEquals("swipeRight", swipeName(Offset(THRESHOLD, 0f), THRESHOLD))
    }
}
