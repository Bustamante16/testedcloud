package com.testedcloud.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.testedcloud.chat.ui.auth.AuthScreen
import com.testedcloud.chat.ui.chat.ChatHomeScreen
import com.testedcloud.chat.ui.theme.TestedCloudChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestedCloudChatTheme {
                var currentUser by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser)
                }

                if (currentUser == null) {
                    AuthScreen(
                        onAuthenticated = {
                            currentUser = FirebaseAuth.getInstance().currentUser
                        }
                    )
                } else {
                    ChatHomeScreen(
                        user = currentUser!!,
                        onSignedOut = {
                            currentUser = null
                        }
                    )
                }
            }
        }
    }
}
