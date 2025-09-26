
package com.example.nougatbora

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.net.UnknownHostException

class ProductsVM(private val repository: AuthRepository) : ViewModel() {
    val products = MutableLiveData<List<ProductResponse>>()
    val errorMessage = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()

    fun getProducts(driverId: String, token: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                errorMessage.postValue(null) // Clear previous errors

                Log.d("ProductsVM", "=== API CALL START ===")
                Log.d("ProductsVM", "URL: https://warehouse-backend-ru6r.onrender.com/api/drivers/$driverId")
                Log.d("ProductsVM", "Driver ID: '$driverId'")
                Log.d("ProductsVM", "Token preview: ${token.take(20)}...")

                // Add coroutine timeout as backup (90 seconds)
                val driver = withTimeout(90000) {
                    repository.getDriverById(driverId, token)
                }

                Log.d("ProductsVM", "✅ API SUCCESS!")
                Log.d("ProductsVM", "Driver name: ${driver.name}")
                Log.d("ProductsVM", "Products count: ${driver.products.size}")

                products.postValue(driver.products)

            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("ProductsVM", "❌ TIMEOUT after 90 seconds")
                errorMessage.postValue("Server is very slow. Please try again later.")

            } catch (e: UnknownHostException) {
                Log.e("ProductsVM", "❌ NO INTERNET")
                errorMessage.postValue("No internet connection. Check your network.")

            } catch (e: HttpException) {
                Log.e("ProductsVM", "❌ HTTP ERROR: ${e.code()}")
                when (e.code()) {
                    404 -> {
                        Log.e("ProductsVM", "Driver not found with ID: '$driverId'")
                        errorMessage.postValue("Driver profile not found. Contact support.")
                    }
                    401 -> {
                        Log.e("ProductsVM", "Authentication failed")
                        errorMessage.postValue("Login expired. Please login again.")
                    }
                    500 -> {
                        Log.e("ProductsVM", "Server error")
                        errorMessage.postValue("Server error. Try again later.")
                    }
                    else -> {
                        errorMessage.postValue("Network error (${e.code()}). Try again.")
                    }
                }

            } catch (e: Exception) {
                Log.e("ProductsVM", "❌ UNEXPECTED ERROR", e)
                errorMessage.postValue("Unexpected error: ${e.message}")

            } finally {
                isLoading.postValue(false)
            }
        }
    }
}
