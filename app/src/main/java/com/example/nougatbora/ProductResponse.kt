package com.example.nougatbora

import com.google.gson.annotations.SerializedName

data class ProductResponse(
   @SerializedName("id") val UserId: String,
   val name: String,
   val quantity: Int,   // stock from backend
   var count: Int

)


