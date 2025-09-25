package com.example.nougatbora

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface QuantitiesDAO {
    @Query("SELECT * FROM quantity WHERE id = 1")
    suspend fun getQuantities(): Quantities?
    @Query("UPDATE quantity SET produita = produita - :qty WHERE id = 1 AND produita >= :qty")
    suspend fun subtractProduita(qty: Int)

    @Query("UPDATE quantity SET produitb = produitb - :qty WHERE id = 1 AND produitb >= :qty")
    suspend fun subtractProduitb(qty: Int)

    @Query("UPDATE quantity SET produitc = produitc - :qty WHERE id = 1 AND produitc >= :qty")
    suspend fun subtractProduitc(qty: Int)

    @Query("UPDATE quantity SET produitd = produitd - :qty WHERE id = 1 AND produitd >= :qty")
    suspend fun subtractProduitd(qty: Int)

    @Query("UPDATE quantity SET produite = produite - :qty WHERE id = 1 AND produite >= :qty")
    suspend fun subtractProduite(qty: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuantities(quantities: Quantities)

    @Update
    suspend fun updateQuantities(quantities: Quantities)

    // This function checks stock and performs the transaction
    @Transaction // This annotation ensures everything inside is a single transaction.
    suspend fun processDeliveryAndUpdateStock(
        delivered1: Int,
        delivered2: Int,
        delivered3: Int,
        delivered4: Int,
        delivered5: Int,
        currentStock: Quantities // Pass the current stock object to check against
    ): Boolean { // Returns TRUE if successful, FALSE if failed (due to insufficient stock)

        // 1. CHECK STOCK FIRST
        if (delivered1 > currentStock.produita ||
            delivered2 > currentStock.produitb ||
            delivered3 > currentStock.produitc ||
            delivered4 > currentStock.produitd ||
            delivered5 > currentStock.produite) {
            return false // Fail the transaction immediately
        }

        // 2. If checks pass, PERFORM THE SUBTRACTIONS
        if (delivered1 > 0) subtractProduita(delivered1)
        if (delivered2 > 0) subtractProduitb(delivered2)
        if (delivered3 > 0) subtractProduitc(delivered3)
        if (delivered4 > 0) subtractProduitd(delivered4)
        if (delivered5 > 0) subtractProduite(delivered5)

        return true // Success
    }
}