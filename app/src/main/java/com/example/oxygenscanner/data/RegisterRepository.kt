package com.example.oxygenscanner.data

import com.example.oxygenscanner.data.model.User

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class RegisterRepository(private val dataSource: RegisterDataSource) {
    suspend fun register(user: User): Result<User> {
        // handle login
        return dataSource.register(user)
    }

    suspend fun checkMobileExist(mobileNumber: String): Result<Boolean> {
        // handle login
        return dataSource.checkMobileExist(mobileNumber)
    }

}