package com.testedcloud.chat.data.users

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createOrUpdateUserProfile(user: FirebaseUser) {
        val userRef = db.collection("users").document(user.uid)
        val snapshot = userRef.get().await()

        val now = Timestamp.now()

        if (snapshot.exists()) {
            userRef.update(
                mapOf(
                    "lastLoginAt" to now,
                    "updatedAt" to now
                )
            ).await()
        } else {
            val profile = mapOf(
                "uid" to user.uid,
                "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: "TestedCloud User"),
                "email" to (user.email ?: ""),
                "photoUrl" to user.photoUrl?.toString(),
                "createdAt" to now,
                "lastLoginAt" to now,
                "updatedAt" to now,
                "status" to "active"
            )

            userRef.set(profile).await()
        }
    }
}
