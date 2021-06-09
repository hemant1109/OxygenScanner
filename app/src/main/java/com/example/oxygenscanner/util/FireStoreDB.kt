package com.example.oxygenscanner.util

import com.example.oxygenscanner.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FireStoreDB {

    private fun getDatabase(): FirebaseFirestore {
        // Write a message to the database
        return FirebaseFirestore.getInstance()
    }

    suspend fun registerUser(user:User): DocumentReference? {
        return getDatabase().collection("users").add(user).await()
    }
}