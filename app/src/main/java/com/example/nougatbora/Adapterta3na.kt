package com.example.nougatbora

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.squareup.picasso.Picasso

class Adapterta3na(
    val context: Context,
    var data: ArrayList<Model>
) : RecyclerView.Adapter<viewholderta3na>() {
     override fun onCreateViewHolder(
         parent: ViewGroup,
         viewType: Int
     ): viewholderta3na { return viewholderta3na(
         LayoutInflater.from(context).inflate(R.layout.listitems, parent, false))
     }

     override fun getItemCount(): Int {
         return data.size

     }

     override fun onBindViewHolder(
         holder: viewholderta3na,
         position: Int
     ) {
         holder.localisation.text = data[position].localisationn
         holder.grocery.text = data[position].groceryy
         holder.recunumber.text = data[position].recunumberr
         holder.kolech.text = "x${data[position].kolechh}"
         holder.produita.text = "x${data[position].produit1}"
         holder.produitb.text = "x${data[position].produitb2}"
         holder.produitc.text = "x${data[position].produitc3}"
         holder.produitd.text = "x${data[position].produitd4}"
         holder.produite.text = "x${data[position].produit5}"
         holder.wa9tt.text = data[position].dateTime

         val item = data[position]

         holder.details.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

         holder.itemView.setOnClickListener {
             item.isExpanded = true
             notifyItemChanged(position)
         }

         holder.bottona.setOnClickListener {
             item.isExpanded = false
             notifyItemChanged(position)
         }
         holder.recuuu.load(data[position].recuu) {
             crossfade(true)
         }

         fun Int.dpToPx(context: Context): Int {
             return (this * context.resources.displayMetrics.density).toInt()
         }

         holder.recuuu.setOnClickListener {
             val dialog = Dialog(context)
             dialog.setContentView(R.layout.dialog_image_preview)

             val imageView = dialog.findViewById<ImageView>(R.id.imagePreview)
             imageView.setImageURI(Uri.parse(data[position].recuu))

             dialog.window?.setLayout(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT
             )
             dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#33000000")))

             imageView.setOnClickListener { dialog.dismiss() }
             dialog.show()
         }




     }

        fun updateData(newList: List<Model>) {
            data.clear()
            data.addAll(newList)
            notifyDataSetChanged()
        }




 }