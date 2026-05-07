package com.testedcloud.chat.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.testedcloud.chat.data.conversations.ChatMessage
import com.testedcloud.chat.data.conversations.ConversationRepository
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    currentUserId: String,
    conversationId: String,
    conversationRepository: ConversationRepository = ConversationRepository(),
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val messages by conversationRepository
        .observeMessages(conversationId)
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }

            Text(
                text = "Conversation",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = conversationId,
            style = MaterialTheme.typography.bodySmall
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    isOwnMessage = message.senderId == currentUserId
                )
            }
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    status = try {
                        conversationRepository.sendMessage(
                            conversationId = conversationId,
                            senderId = currentUserId,
                            text = messageText
                        )
                        messageText = ""
                        "Message sent"
                    } catch (e: Exception) {
                        "Send failed: ${e.message}"
                    }
                }
            },
            enabled = messageText.trim().isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }

        if (status.isNotBlank()) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: ChatMessage,
    isOwnMessage: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = if (isOwnMessage) "You" else "Other user",
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
