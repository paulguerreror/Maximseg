package com.neobit.maximseg.adapters

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.neobit.maximseg.R
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.data.model.Guardia
import com.neobit.maximseg.data.model.dano
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_main.*

class GuardiaAdapter(val guardias:ArrayList<Guardia>): androidx.recyclerview.widget.RecyclerView.Adapter<GuardiaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.guardias,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return guardias.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(guardias[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return guardias[position].id_guardia.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(guardia: Guardia) {
            val cdguardia = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.card_guardia)
            val tvguardia = itemView.findViewById<TextView>(R.id.tvguardia)
            val imgguardia = itemView.findViewById<ImageView>(R.id.imgguardia)
            cdguardia.setTag(guardia.id_guardia.toString())
            tvguardia.text = guardia.nombres
            Picasso.get().load(Utils.URL_MEDIA + guardia.imagen).error(R.drawable.men).placeholder(R.drawable.men).noFade().into(imgguardia)
        }
    }
}