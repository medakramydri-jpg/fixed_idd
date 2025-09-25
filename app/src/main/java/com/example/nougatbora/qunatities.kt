package com.example.nougatbora

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quantity")
data class Quantities(
    @PrimaryKey val id: Int = 1,
    var produita: Int,
    var produitb: Int,
    var produitc: Int,
    var produitd: Int,
    var produite: Int
)
