package com.example.nougatbora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductsVMFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsVM::class.java)) {
            return ProductsVM(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
