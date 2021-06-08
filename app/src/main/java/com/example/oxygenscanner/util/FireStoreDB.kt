package com.example.oxygenscanner.util

import com.example.oxygenscanner.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FireStoreDB {

    private fun getDatabase(): DatabaseReference {
        // Write a message to the database
        val database = Firebase.database
        return database.reference
    }

    suspend fun registerUser(user:User): Void? {
        return getDatabase().child("users").setValue(user).await()
    }
}