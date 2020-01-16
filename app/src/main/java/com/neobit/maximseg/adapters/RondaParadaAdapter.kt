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
import com.neobit.maximseg.data.model.RondaParada

class RondaParadaAdapter(val rondaparadas:ArrayList<RondaParada>):
    androidx.recyclerview.widget.RecyclerView.Adapter<RondaParadaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.rondaparadas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return rondaparadas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(rondaparadas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return rondaparadas[position].id_ronda.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(rondaparada: RondaParada) {
            val txtParadaFecha = itemView.findViewById<TextView>(R.id.txtParadaFecha)
            val txtParadaNombre = itemView.findViewById<TextView>(R.id.txtParadaNombre)
            txtParadaNombre.text = rondaparada.nombre
            txtParadaFecha.text = rondaparada.hora
        }
    }
}