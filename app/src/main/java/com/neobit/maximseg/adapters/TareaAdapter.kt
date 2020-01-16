package com.neobit.maximseg.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.neobit.maximseg.R
import com.neobit.maximseg.data.model.Tarea

class TareaAdapter(val tareas:ArrayList<Tarea>): RecyclerView.Adapter<TareaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.tareas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return tareas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(tareas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return tareas[position].id_tarea.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(tarea: Tarea) {
            val tarea_descripcion = itemView.findViewById<TextView>(R.id.tarea_descripcion)
            val btnTarea = itemView.findViewById<ImageView>(R.id.btnTarea)
            btnTarea.setTag(tarea.id_tarea.toString())
            tarea_descripcion.text = tarea.descripcion
        }


    }
}