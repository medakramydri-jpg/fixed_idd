package com.example.nougatbora


import androidx.lifecycle.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authResult = MutableLiveData<Result<AuthResponse>>()
    val authResult: LiveData<Result<AuthResponse>> get() = _authResult

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            try {
                val response = repository.register(request)
                _authResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _authResult.postValue(Result.failure(e))
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                _authResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _authResult.postValue(Result.failure(e))
            }
        }
    }
}
