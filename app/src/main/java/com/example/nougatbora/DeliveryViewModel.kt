package com.example.nougatbora

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import kotlinx.coroutines.launch

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.DatabaseProvider.getDb(application).deliveryDao()
    val allDeliveries = dao.getAllDeliveries()

    fun insert(delivery: Model) = viewModelScope.launch {
        dao.insert(delivery)
    }
}
