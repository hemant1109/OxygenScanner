package com.example.oxygenscanner.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oxygenscanner.data.RegisterRepository
import com.example.oxygenscanner.data.Result
import com.example.oxygenscanner.data.model.User
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {


    private val _registerLiveData = MutableLiveData<Result<User>>()
    val registerLiveData: LiveData<Result<User>> = _registerLiveData

    fun register(user: User) {
        viewModelScope.launch {
            _registerLiveData.postValue(registerRepository.register(user))
        }
    }
}