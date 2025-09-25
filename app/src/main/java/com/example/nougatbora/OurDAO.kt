package com.example.nougatbora

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OurDAO {
    @Insert
    suspend fun insert(delivery: Model)

    @Query("SELECT * FROM deliveries ORDER BY id DESC")
    fun getAllDeliveries(): LiveData<List<Model>>


}



