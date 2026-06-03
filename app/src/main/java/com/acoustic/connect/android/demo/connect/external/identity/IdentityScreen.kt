/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any
 * intellectual or industrial property rights of Acoustic, L.P. except as may
 * be provided in an agreement with Acoustic, L.P. Any unauthorized copying or
 * distribution of content from this file is prohibited.
 */
package com.acoustic.connect.android.demo.connect.external.identity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedButton
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedOutlinedTextField
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticGreen
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticPurple
import com.acoustic.connect.android.demo.connect.external.ui.theme.LightCard

@Composable
fun IdentityScreen(
    viewModel: IdentityViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshSdkEnabled()
        onPauseOrDispose { }
    }

    IdentityContent(
        state = uiState,
        modifier = modifier,
        onIdentifierNameChanged = viewModel::onIdentifierNameChanged,
        onIdentifierValueChanged = viewModel::onIdentifierValueChanged,
        onLogIdentity = viewModel::onLogIdentity,
        onHistoryEntrySelected = viewModel::onHistoryEntrySelected,
    )
}

@Composable
private fun IdentityContent(
    state: IdentityUiState,
    modifier: Modifier = Modifier,
    onIdentifierNameChanged: (String) -> Unit,
    onIdentifierValueChanged: (String) -> Unit,
    onLogIdentity: () -> Unit,
    onHistoryEntrySelected: (IdentityHistoryEntry) -> Unit,
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
            Spacer(modifier = Modifier.height(40.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            IdentityCard(
                identifierName = state.identifierName,
                identifierValue = state.identifierValue,
                statusMessage = state.statusMessage,
                isSuccess = state.isSuccess,
                isSdkEnabled = state.isSdkEnabled,
                onIdentifierNameChanged = onIdentifierNameChanged,
                onIdentifierValueChanged = onIdentifierValueChanged,
                onLogIdentity = onLogIdentity,
            )

            if (state.history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                HistoryCard(
                    history = state.history,
                    onEntrySelected = onHistoryEntrySelected,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun IdentityCard(
    identifierName: String,
    identifierValue: String,
    statusMessage: String,
    isSuccess: Boolean,
    isSdkEnabled: Boolean,
    onIdentifierNameChanged: (String) -> Unit,
    onIdentifierValueChanged: (String) -> Unit,
    onLogIdentity: () -> Unit,
) {
    val noAutocorrect = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoggedText(
                text = "Log Identity",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { testTag = "tv_log_identity_title" },
            )

            LoggedOutlinedTextField(
                value = identifierName,
                onValueChange = onIdentifierNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "et_identifier_name" },
                label = { Text("Identifier Name") },
                placeholder = { Text("e.g. Email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = noAutocorrect,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AcousticPurple,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                ),
                maskLabel = "Identifier Name",
            )

            LoggedOutlinedTextField(
                value = identifierValue,
                onValueChange = onIdentifierValueChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "et_identifier_value" },
                label = { Text("Identifier Value") },
                placeholder = { Text("e.g. user@example.com") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = noAutocorrect.copy(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AcousticPurple,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                ),
                maskLabel = "Identifier Value",
            )

            Spacer(modifier = Modifier.height(4.dp))

            LoggedButton(
                onClick = onLogIdentity,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "btn_send_identity_signal" },
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcousticPurple,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White,
                ),
                buttonText = "Send Identity Signal",
            ) {
                Text("Send Identity Signal")
            }

            if (statusMessage.isNotEmpty()) {
                LoggedText(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSuccess) AcousticGreen else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    history: List<IdentityHistoryEntry>,
    onEntrySelected: (IdentityHistoryEntry) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LoggedText(
                text = "Recent",
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            history.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(color = Color(0xFFE5E5EA))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEntrySelected(entry) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LoggedText(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    LoggedText(
                        text = entry.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B6B6B),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                }
            }
        }
    }
}