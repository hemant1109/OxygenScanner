package com.app.oxygenscanner.data

import com.app.oxygenscanner.data.model.User
import com.app.oxygenscanner.util.FireStoreDB
import java.io.IOException


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class RegisterDataSource {

    suspend fun register(user: User): Result<User> {
        return try {
            FireStoreDB.registerUser(user)
            Result.Success(user)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.Error(IOException("Error logging in", e))
        }
    }

    suspend fun checkMobileExist(mobileNumber: String): Result<Boolean>  {
        return try {
            Result.Success(FireStoreDB.checkMobileExist(mobileNumber))
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}