package com.app.oxygenscanner.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.oxygenscanner.data.LoginDataSource
import com.app.oxygenscanner.data.LoginRepository
import com.app.oxygenscanner.data.RegisterDataSource
import com.app.oxygenscanner.data.RegisterRepository
import com.app.oxygenscanner.ui.login.LoginViewModel
import com.app.oxygenscanner.ui.register.RegisterViewModel

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource()
                )
            ) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(
                registerRepository = RegisterRepository(
                    dataSource = RegisterDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}