/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.acoustic.connect.android.demo.connect.external.gestures.GestureScreen
import com.acoustic.connect.android.demo.connect.external.identity.IdentityScreen
import com.acoustic.connect.android.demo.connect.external.notification.NotificationScreen
import com.acoustic.connect.android.demo.connect.external.notification.NotificationViewModel
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticPurple

private const val ROUTE_NOTIFICATION = "notification_screen"
private const val ROUTE_IDENTITY = "identity_screen"

/**
 * Third route so route changes are observable as a sequence rather than a two-tab toggle, and so the
 * gesture targets live on a screen of their own. Name matches the XML sample app.
 */
private const val ROUTE_GESTURES = "gestures_screen"

private data class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTagId: String,
)

private val tabs = listOf(
    Tab(route = ROUTE_NOTIFICATION, label = "Notification", icon = Icons.Filled.Notifications, testTagId = "tab_notification"),
    Tab(route = ROUTE_IDENTITY, label = "Identity", icon = Icons.Filled.Person, testTagId = "tab_identity"),
    Tab(route = ROUTE_GESTURES, label = "Gestures", icon = Icons.Filled.TouchApp, testTagId = "tab_gestures"),
)

@Composable
fun MainScreen(navController: NavHostController, notificationViewModel: NotificationViewModel) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.semantics {
            testTagsAsResourceId = true
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(ROUTE_NOTIFICATION) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AcousticPurple,
                            selectedTextColor = AcousticPurple,
                            indicatorColor = Color(0xFFF0EEFF),
                            unselectedIconColor = Color(0xFF9E9E9E),
                            unselectedTextColor = Color(0xFF9E9E9E),
                        ),
                        modifier = Modifier.semantics { testTag = tab.testTagId },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_NOTIFICATION,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_NOTIFICATION) {
                NotificationScreen(
                    viewModel = notificationViewModel,
                )
            }
            composable(ROUTE_IDENTITY) {
                IdentityScreen()
            }
            composable(ROUTE_GESTURES) {
                GestureScreen()
            }
        }
    }
}
