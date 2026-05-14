package com.testedcloud.chat.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.testedcloud.chat.auth.AuthRepository
import com.testedcloud.chat.data.conversations.Conversation
import com.testedcloud.chat.data.conversations.ConversationRepository
import kotlinx.coroutines.launch

@Composable
fun ChatHomeScreen(
    user: FirebaseUser,
    authRepository: AuthRepository = AuthRepository(),
    conversationRepository: ConversationRepository = ConversationRepository(),
    onSignedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var otherUserEmail by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val conversations by conversationRepository
        .observeConversations(user.uid)
        .collectAsState(initial = emptyList())

    if (selectedConversationId != null) {
        ChatScreen(
            currentUserId = user.uid,
            conversationId = selectedConversationId!!,
            onBack = { selectedConversationId = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TestedCloud Chat",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Signed in as: ${user.email ?: "unknown"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Your UID: ${user.uid}",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(
            onClick = {
                authRepository.signOut()
                onSignedOut()
            }
        ) {
            Text("Sign out")
        }

        HorizontalDivider()

        Text(
            text = "Create direct conversation",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = otherUserEmail,
            onValueChange = { otherUserEmail = it.trim().lowercase() },
            label = { Text("Other user email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                scope.launch {
                    status = try {
                        val conversationId = conversationRepository.createDirectConversationByEmail(
                            currentUserId = user.uid,
                            otherUserEmail = otherUserEmail
                        )
                        otherUserEmail = ""
                        selectedConversationId = conversationId
                        "Conversation ready: $conversationId"
                    } catch (e: Exception) {
                        "Create conversation failed: ${e.message}"
                    }
                }
            },
            enabled = otherUserEmail.isNotBlank() && otherUserEmail != user.email,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create conversation")
        }

        if (status.isNotBlank()) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider()

        Text(
            text = "Conversations",
            style = MaterialTheme.typography.titleMedium
        )

        if (conversations.isEmpty()) {
            Text("No conversations yet.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        currentUserId = user.uid,
                        onClick = { selectedConversationId = conversation.conversationId },
                        onDelete = {
                            scope.launch {
                                status = try {
                                    conversationRepository.deleteConversationForUser(
                                        conversationId = conversation.conversationId,
                                        currentUserId = user.uid
                                    )
                                    "Conversation deleted for you"
                                } catch (e: Exception) {
                                    "Delete conversation failed: ${e.message}"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val otherParticipants = conversation.participantIds.filter { it != currentUserId }

    val otherUserLabels = otherParticipants.map { participantId ->
        conversation.participantDisplayNames[participantId]
            ?: conversation.participantEmails[participantId]
            ?: participantId
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Direct chat",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "With: ${otherUserLabels.joinToString()}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = conversation.lastMessageText.ifBlank { "No messages yet" },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDelete
            ) {
                Text("Delete conversation")
            }
        }
    }
}
