package com.neobit.maximseg.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.neobit.maximseg.R
import com.neobit.maximseg.data.model.Ronda

class RondaAdapter(val rondas:ArrayList<Ronda>):
    androidx.recyclerview.widget.RecyclerView.Adapter<RondaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.rondas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return rondas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(rondas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return rondas[position].id_ronda.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(ronda: Ronda) {
            val txtronda = itemView.findViewById<TextView>(R.id.txtronda)
            val btnRondaQr = itemView.findViewById<ImageButton>(R.id.btnRondaQr)
            btnRondaQr.setTag(ronda.id_ronda.toString())
            txtronda.text = ronda.nombre
        }
    }
}