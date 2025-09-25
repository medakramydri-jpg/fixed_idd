package com.example.nougatbora

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class viewholderta3na (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val localisation : TextView = itemView.findViewById(R.id.placa)
    val grocery : TextView = itemView.findViewById(R.id.superette)
    val recunumber : TextView = itemView.findViewById(R.id.reference)
    val kolech : TextView = itemView.findViewById(R.id.quantity)
    val produita : TextView = itemView.findViewById(R.id.quantitya)
    val produitb : TextView = itemView.findViewById(R.id.quantityb)
    val produitc : TextView = itemView.findViewById(R.id.quantityc)
    val produitd : TextView = itemView.findViewById(R.id.quantityd)
    val produite : TextView = itemView.findViewById(R.id.quantitye)
    val details : LinearLayout = itemView.findViewById(R.id.habat)
    val bottona : ImageView = itemView.findViewById(R.id.tala3)
    val recuuu: ImageView = itemView.findViewById(R.id.lfatouraa)
    val wa9tt : TextView = itemView.findViewById(R.id.wa9t)

}