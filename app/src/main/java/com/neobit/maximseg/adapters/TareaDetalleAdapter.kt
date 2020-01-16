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
import com.neobit.maximseg.data.model.Tarea
import com.neobit.maximseg.data.model.TareaDetalle

class TareaDetalleAdapter(val tareadetalles:ArrayList<TareaDetalle>):
    androidx.recyclerview.widget.RecyclerView.Adapter<TareaDetalleAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.tareadetalles,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return tareadetalles.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(tareadetalles[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return tareadetalles[position].id_punto_tarea_detalle.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(tareadetalle: TareaDetalle) {
            val txtTareaDetalle = itemView.findViewById<TextView>(R.id.txtTareaDetalle)
            val btnTareaDetalle = itemView.findViewById<ImageButton>(R.id.btnTareaDetalle)
            val ckTareaDetalle = itemView.findViewById<CheckBox>(R.id.ckTareaDetalle)

            if(tareadetalle.checked == "1"){
                ckTareaDetalle.isChecked = true
            }else if(tareadetalle.checked == "0"){
                ckTareaDetalle.isChecked = false
            }
            if(tareadetalle.info == 1){
                ckTareaDetalle.visibility = View.GONE
                btnTareaDetalle.visibility = View.GONE
                txtTareaDetalle.gravity = Gravity.END or Gravity.BOTTOM
            }
            ckTareaDetalle.setTag(tareadetalle.id_punto_tarea_detalle.toString())
            btnTareaDetalle.setTag(tareadetalle.id_punto_tarea_detalle.toString())
            txtTareaDetalle.text = tareadetalle.descripcion
        }


    }
}