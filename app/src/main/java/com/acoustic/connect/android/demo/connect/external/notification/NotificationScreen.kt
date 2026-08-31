/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external.notification

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedButton
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticGreen
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticPurple
import com.acoustic.connect.android.demo.connect.external.ui.theme.LightCard

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? ComponentActivity

    ScreenviewUnloadEffect("notification_screen")

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAuthorization()
        onPauseOrDispose { }
    }

    NotificationContent(
        state = uiState,
        modifier = modifier,
        onRequestAuthorization = {
            activity?.let { Connect.push.requestNotificationPermission(it) }
        },
    )
}

@Composable
private fun NotificationContent(
    state: NotificationUiState,
    modifier: Modifier = Modifier,
    onRequestAuthorization: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .semantics { testTagsAsResourceId = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.acoustic_logo_light),
                contentDescription = "Acoustic Connect Logo",
                modifier = Modifier.width(260.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LoggedText(
                text = "Connect Demo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            NotificationAuthorizationCard(
                authorized = state.isNotificationAuthorized,
                statusMessage = state.notificationStatusMessage,
                isSdkEnabled = state.isSdkEnabled,
                onRequestAuthorization = onRequestAuthorization,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NotificationAuthorizationCard(
    authorized: Boolean,
    statusMessage: String = "",
    isSdkEnabled: Boolean,
    onRequestAuthorization: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightCard
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            LoggedText(
                text = "Notification Authorization",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { testTag = "tv_notification_auth_title" },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (authorized) Color(0xFF2ECC71) else Color.Gray,
                            shape = CircleShape
                        )
                        .semantics { testTag = "iv_notification_auth_status_dot" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                LoggedText(
                    text = "Status: ${if (authorized) "Authorized" else "Not Authorized"}",
                    modifier = Modifier.semantics { testTag = "tv_notification_auth_status" },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LoggedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcousticPurple,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White,
                ),
                onClick = onRequestAuthorization,
                enabled = isSdkEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "btn_request_authorization" },
                buttonText = "Enable Push",
            ) {
                Text("Enable Push")
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LoggedText(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        statusMessage.startsWith("Error") -> MaterialTheme.colorScheme.error
                        statusMessage.contains("disabled") -> MaterialTheme.colorScheme.error
                        else -> AcousticGreen
                    },
                )
            }
        }
    }
}
