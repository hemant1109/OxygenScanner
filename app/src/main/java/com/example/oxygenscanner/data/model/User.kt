package com.example.oxygenscanner.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val fullName: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val gender: String? = null
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}