package com.example.nougatbora

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class productsAdapter(
    val context: Context,
    var data: ArrayList<ProductResponse>
) : RecyclerView.Adapter<OneProductViewholder>() {

    // 🔹 Expose current product list to the Activity
    val currentData: List<ProductResponse>
        get() = data

    // 🔹 Callback for when any product count changes
    var onProductCountChanged: (() -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OneProductViewholder {
        return OneProductViewholder(
            LayoutInflater.from(context).inflate(R.layout.rowproduct, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(
        holder: OneProductViewholder,
        position: Int
    ) {
        val item = data[position]

        holder.productname.text = item.name
        holder.count.text = item.count.toString()

        holder.zid.setOnClickListener {
            item.count++
            holder.count.text = item.count.toString()
            onProductCountChanged?.invoke() // 🔔 notify activity
        }

        holder.na9as.setOnClickListener {
            if (item.count > 0) {
                item.count--
                holder.count.text = item.count.toString()
                onProductCountChanged?.invoke() // 🔔 notify activity
            }
        }
    }

    fun updateList(newData: ArrayList<ProductResponse>) {
        data = newData
        notifyDataSetChanged()
        onProductCountChanged?.invoke() // 🔔 trigger after full refresh
    }
}
