package com.neobit.maximseg.adapters

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.neobit.maximseg.R
import com.neobit.maximseg.data.model.PuntoTarea
import com.neobit.maximseg.data.model.Tarea
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.tareadetalles.*

class PuntoTareaAdapter(val puntotareas:ArrayList<PuntoTarea>):
    androidx.recyclerview.widget.RecyclerView.Adapter<PuntoTareaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.puntotareas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return puntotareas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(puntotareas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return puntotareas[position].id_punto_tarea.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(puntotarea: PuntoTarea) {
            val punto_tarea_descripcion = itemView.findViewById<TextView>(R.id.punto_tarea_descripcion)
            val punto_tarea_fecha = itemView.findViewById<TextView>(R.id.punto_tarea_fecha)
            val btnPuntoTarea = itemView.findViewById<ImageButton>(R.id.btnPuntoTarea)
            if(puntotarea.hora_inicio == ""){
                punto_tarea_fecha.visibility = View.GONE
            }
            if(puntotarea.info==1){
                //btnPuntoTarea.isEnabled = false
                //btnPuntoTarea.isClickable = false
                Picasso.get().load(R.drawable.info).error(R.drawable.info).placeholder(R.drawable.info).noFade().into(btnPuntoTarea)
                val id = "i" + puntotarea.id_punto_tarea.toString()
                btnPuntoTarea.setTag(id)
            }else{
                if(puntotarea.done==1){
                    btnPuntoTarea.isEnabled = false
                    btnPuntoTarea.isClickable = false
                    Picasso.get().load(R.drawable.check_done).error(R.drawable.check_done).placeholder(R.drawable.check_done).noFade().into(btnPuntoTarea)
                }
                btnPuntoTarea.setTag(puntotarea.id_punto_tarea.toString())
            }
            punto_tarea_descripcion.text = puntotarea.nombre
            punto_tarea_fecha.text = puntotarea.hora_inicio
        }


    }
}