package com.example.nougatbora

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class OneProductViewholder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val productname : TextView = itemView.findViewById(R.id.productnamee)
    val count : TextView = itemView.findViewById(R.id.ch7al)
    val zid : LinearLayout = itemView.findViewById(R.id.p)
    val na9as : LinearLayout = itemView.findViewById(R.id.n)
}