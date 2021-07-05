package com.app.oxygenscanner.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.oxygenscanner.data.RegisterRepository
import com.app.oxygenscanner.data.Result
import com.app.oxygenscanner.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {


    private val _registerLiveData = MutableLiveData<Result<User>>()
    val registerLiveData: LiveData<Result<User>> = _registerLiveData

    fun register(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerLiveData.postValue(registerRepository.register(user))
        }
    }
    private val _mobileExist = MutableLiveData<Result<Boolean>>()
    val mobileExist: LiveData<Result<Boolean>> = _mobileExist

    fun checkMobileExist(mobileNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _mobileExist.postValue(registerRepository.checkMobileExist(mobileNumber))
        }
    }

}