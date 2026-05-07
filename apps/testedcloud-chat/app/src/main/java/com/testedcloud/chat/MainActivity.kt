package com.testedcloud.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.testedcloud.chat.ui.auth.AuthScreen
import com.testedcloud.chat.ui.theme.TestedCloudChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestedCloudChatTheme {
                AuthScreen()
            }
        }
    }
}
