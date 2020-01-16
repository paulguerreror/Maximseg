package com.neobit.maximseg.adapters

import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.neobit.maximseg.R
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.data.model.Aviso
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AvisoAdapter(val avisos:ArrayList<Aviso>): RecyclerView.Adapter<AvisoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.avisos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return avisos.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(avisos[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return avisos[position].id_aviso.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(aviso: Aviso) {
            val btnCheckAviso = itemView.findViewById<ImageButton>(R.id.btnCheckAviso)
            val txtAvisoDescripcion = itemView.findViewById<TextView>(R.id.txtAvisoDescripcion)
            val txtAvisoFecha = itemView.findViewById<TextView>(R.id.txtAvisoFecha)
            //val time = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            txtAvisoDescripcion.text = aviso.descripcion
            txtAvisoFecha.text = aviso.hora
            btnCheckAviso.setTag(R.id.id,aviso.id_aviso_historial)

            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getTimeInstance() //or use getDateInstance()
            val formatedDate = formatter.format(date)
            val myTime = aviso.hora
            val df = SimpleDateFormat("HH:mm")
            val d = df.parse(myTime)
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MINUTE, 15)
            val time = df.format(cal.time)


            if(aviso.validado == 1){
                btnCheckAviso.isClickable = false
                Picasso.get().load(R.drawable.check_done).error(R.drawable.ic_alert).placeholder(R.drawable.ic_alert).noFade().into(btnCheckAviso)
            }else if(formatedDate.compareTo(time) > 0){
                btnCheckAviso.isClickable = false
                Picasso.get().load(R.drawable.ic_error).error(R.drawable.ic_alert).placeholder(R.drawable.ic_alert).noFade().into(btnCheckAviso)
            }
        }


    }
}