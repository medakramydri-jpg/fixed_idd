package com.example.nougatbora

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsVM(private val repository: AuthRepository) : ViewModel() {
    val products = MutableLiveData<List<ProductResponse>>()
    val errorMessage = MutableLiveData<String?>()

    fun getProducts(driverId: String, token: String) {
        viewModelScope.launch {
            try {
                val driver = repository.getDriverById(driverId, token)
                products.postValue(driver.products)
            } catch (e: Exception) {
                errorMessage.postValue(e.message ?: "Unknown error")
            }
        }
    }
}


