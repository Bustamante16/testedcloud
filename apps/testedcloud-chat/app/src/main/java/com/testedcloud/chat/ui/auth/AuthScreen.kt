package com.testedcloud.chat.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.testedcloud.chat.auth.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository = AuthRepository()
) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf(
            authRepository.currentUser?.email?.let { "Signed in as $it" }
                ?: "Not signed in"
        )
    }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TestedCloud Chat",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Firebase Authentication MVP",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            status = try {
                                val user = authRepository.signIn(email, password)
                                "Signed in as ${user?.email ?: "unknown user"}"
                            } catch (e: Exception) {
                                "Sign in failed: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Working..." else "Sign in")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            status = try {
                                val user = authRepository.createAccount(email, password)
                                "Account created: ${user?.email ?: "unknown user"}"
                            } catch (e: Exception) {
                                "Create account failed: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.length >= 6,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create account")
                }

                OutlinedButton(
                    onClick = {
                        authRepository.signOut()
                        status = "Signed out"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign out")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
