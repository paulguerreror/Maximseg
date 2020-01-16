package com.neobit.maximseg.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.neobit.maximseg.R
import com.neobit.maximseg.data.model.RondaTarea

class RondaTareaAdapter(val rondatareas:ArrayList<RondaTarea>):
    androidx.recyclerview.widget.RecyclerView.Adapter<RondaTareaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.rondatareas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return rondatareas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(rondatareas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return rondatareas[position].id_ronda_tarea.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(rondatarea: RondaTarea) {
            val txtRondaTarea = itemView.findViewById<TextView>(R.id.txtRondaTarea)
            val ckRondaTarea = itemView.findViewById<CheckBox>(R.id.ckRondaTarea)
            val btnRondaTarea = itemView.findViewById<ImageButton>(R.id.btnRondaTarea)
            if(rondatarea.checked == "1"){
                ckRondaTarea.isChecked = true
            }else if(rondatarea.checked == "0"){
                ckRondaTarea.isChecked = false
            }
            ckRondaTarea.setTag(rondatarea.id_ronda_tarea.toString())
            btnRondaTarea.setTag(rondatarea.id_ronda_tarea.toString())
            txtRondaTarea.text = rondatarea.nombre
        }


    }
}