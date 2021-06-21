package com.example.oxygenscanner.util

import com.example.oxygenscanner.data.model.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FireStoreDB {

    private fun getDatabase(): FirebaseFirestore {
        // Write a message to the database
        return FirebaseFirestore.getInstance()
    }

    suspend fun registerUser(user: User): DocumentReference? {
        return getDatabase().collection("users").add(user).await()
    }

    suspend fun checkMobileExist(phoneNum: String): Boolean {
        return !getDatabase().collection("users").whereEqualTo("mobileNumber", phoneNum).get()
            .await().isEmpty
    }
}