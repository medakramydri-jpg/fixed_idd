package com.example.nougatbora

import android.widget.ImageView
import android.widget.TextView
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class Model(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val localisationn : String,
    val groceryy : String,
    val recunumberr : String,
    val kolechh : String,
    val produit1 : String,
    val produitb2 : String,
    val produitc3 : String,
    val produitd4 : String,
    val produit5 : String,
    val recuu : String,
    val dateTime: String,
    var isExpanded: Boolean = false

)
