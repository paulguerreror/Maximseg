package com.neobit.maximseg

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.PuntoTareaAdapter
import com.neobit.maximseg.adapters.RondaTareaAdapter
import com.neobit.maximseg.adapters.TareaDetalleAdapter
import com.neobit.maximseg.data.model.PuntoTarea
import com.neobit.maximseg.data.model.RondaTarea
import com.neobit.maximseg.data.model.TareaDetalle
import kotlinx.android.synthetic.main.activity_tareas.*
import kotlinx.android.synthetic.main.fragment_tareas_generales.*

import kotlinx.android.synthetic.main.tareadetalles.txtTareaDetalle

import org.json.JSONObject
import java.util.HashMap

class TareasActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var id_punto_tarea_detalles: String
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var detalles: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)
        val actionbar = supportActionBar
        actionbar!!.title= resources.getString(R.string.title_tareas)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getTareaDetalles()
        /*
        if(prefs.contains("id_punto_tarea_detalles") && !prefs.contains("info")){
            Log.d("existe",prefs.getString("id_punto_tarea_detalles",""))
            getTareaDetalles2()
        }else{
            getTareaDetalles()
            Log.d("existe","no existe")
        }
         */
        saveTareaDetalle.setOnClickListener {
            saveTareaDetalle()
        }
    }

    override fun onBackPressed() {
        if(prefs.contains("id_tarea_historial")){
            Toast.makeText(applicationContext, resources.getString(R.string.error_back_round), Toast.LENGTH_LONG).show()
        }else{
            prefs.edit().remove("id_tarea").commit()
            prefs.edit().remove("id_punto_tarea_detalle").commit()
            prefs.edit().remove("id_tarea_historial").commit()
            prefs.edit().remove("id_punto_tarea_detalles").commit()
            prefs.edit().remove("checks").commit()
            prefs.edit().remove("imagenes").commit()
            prefs.edit().remove("detalles").commit()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getTareaDetalles() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTareaDetalle)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val tareadetalles = ArrayList<TareaDetalle>()
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this)
            val URL = "${Utils.URL_SERVER}tareas/${prefs.getString("id_tarea","")}/detalles"

            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_punto_tarea_detalles = ""
                    checks= ""
                    imagenes= ""
                    detalles= ""
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("detalles")
                    val tarea = jsonObj.getJSONObject("tarea")
                    val info = tarea.getInt("info")
                    if(!prefs.contains("id_tarea_historial") && info == 0){
                        createHistorialTarea()
                        prefs.edit().remove("info").commit()

                    }else{
                        prefs.edit().putString("info",info.toString())
                    }
                    if(info == 0){
                        saveTareaDetalle.visibility = View.VISIBLE
                    }
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_punto_tarea_detalle = jsonInner.get("id_punto_tarea_detalle").toString().toInt()
                        val descripcion = jsonInner.get("descripcion").toString()
                        Log.d("add","$id_punto_tarea_detalle|$descripcion|$info")
                        tareadetalles.add(TareaDetalle(id_punto_tarea_detalle,descripcion,info,"%"))
                        if(id_punto_tarea_detalles == ""){
                            id_punto_tarea_detalles +=  id_punto_tarea_detalle
                            checks +=  "%"
                        }else{
                            id_punto_tarea_detalles += "|" + jsonInner.get("id_punto_tarea_detalle").toString()
                            checks +=  "|%"
                            imagenes += "|"
                            detalles += "|"
                        }
                        Log.d("id_punto_tarea_detalles",id_punto_tarea_detalles)
                    }
                    val adapter = TareaDetalleAdapter(tareadetalles)

                    recyclerView.adapter = adapter
                    prefs.edit().putString("id_punto_tarea_detalles",id_punto_tarea_detalles).apply()
                    prefs.edit().putString("checks",checks).apply()
                    Log.e("checks",prefs.getString("checks",""))
                    prefs.edit().putString("imagenes",imagenes).apply()
                    prefs.edit().putString("detalles",detalles).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    private fun getTareaDetalles2(){
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTareaDetalle)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val tareadetalles = ArrayList<TareaDetalle>()
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(this)
            val URL = "${Utils.URL_SERVER}tareas/${prefs.getString("id_tarea","")}/detalles"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_punto_tarea_detalles = ""
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("detalles")
                    val tarea = jsonObj.getJSONObject("tarea")
                    val info = tarea.getInt("info")
                    if(info == 0){
                        saveTareaDetalle.visibility = View.VISIBLE
                    }
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_punto_tarea_detalle = jsonInner.get("id_punto_tarea_detalle").toString().toInt()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val checked = getCheck(i)
                        Log.d("add2","$id_punto_tarea_detalle|$descripcion|$info|$checked")
                        tareadetalles.add(TareaDetalle(id_punto_tarea_detalle,descripcion,info,checked))
                    }
                    val adapter = TareaDetalleAdapter(tareadetalles)
                    recyclerView.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    fun getCheck(position: Int):String{
        val Listchecks = prefs.getString("checks", "").split("|")
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                return it
            }
            i++
        }
        return "%"
    }

    fun checkTareaDetalle(view: View){
        Log.e("checks",prefs.getString("checks",""))
        val id = view.getTag().toString()
        var arrayId = prefs.getString("id_punto_tarea_detalles", "").split("|")
        val Listchecks = prefs.getString("checks", "").split("|")
        val Arraychecks = arrayListOf<String>()
        val Listimagenes = prefs.getString("imagenes", "").split("|")
        val Arrayimagenes = arrayListOf<String>()
        val Listdetalles = prefs.getString("detalles", "").split("|")
        val Arraydetalles = arrayListOf<String>()
        val position = arrayId.indexOf(id)
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                if(it.contains("1"))
                    ck = "%"
                else
                    ck = "1"
            }
            Arraychecks.add(ck)
            i++
        }
        checks = Arraychecks.toString().replace("[","").replace("]","").replace("  "," ").replace(", ","|").replace(",","|")
        prefs.edit().putString("checks",checks).apply()
        //imagenes
        i=0
        Listimagenes.forEach{
            var ck = it
            if(i == position){
                ck = ""
            }
            Arrayimagenes.add(ck)
            i++
        }
        imagenes = Arrayimagenes.toString().replace("[","").replace("]","").replace(" ","").replace(",","|")
        prefs.edit().putString("imagenes",imagenes).apply()
        //danos
        i=0
        Listdetalles.forEach{
            var ck = it
            if(i == position){
                ck = ""
            }
            Arraydetalles.add(ck)
            i++
        }
        detalles = Arraydetalles.toString().replace("[","").replace("]","").replace(" ","").replace(",","|")
        prefs.edit().putString("detalles",detalles).apply()
        Log.d("id_punto_tarea_detalles",prefs.getString("id_punto_tarea_detalles", ""))
        Log.d("checks",prefs.getString("checks", ""))
        Log.d("imagenes",prefs.getString("imagenes", ""))
        Log.d("detalles",prefs.getString("detalles", ""))
    }

    fun reportTareaDetalle(view: View){
        prefs.edit().putString("id_punto_tarea_detalle", view.getTag().toString()).apply()
        finish()
        startActivity(Intent(this, ReporteTareaDetalleActivity::class.java))
    }

    fun validate(): Boolean {
        Log.d("validar", "empieza")
        if(prefs.getString("checks","").contains("%")){
            Log.d("validate","faltan datos")
            return true
        }else{
            return false
            Log.d("validate","completo")
        }
    }

    private fun createHistorialTarea() {
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else if(validate()){
            Toast.makeText(applicationContext, R.string.error_fields_required, Toast.LENGTH_LONG).show()
        }else{
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}tareas/${prefs.getString("id_tarea","")}/historial"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {

                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    prefs.edit().putString("id_tarea_historial",jsonObj.getString("id")).apply()
                    Log.d("estado","tarea_historial creado${prefs.getString("id_tarea_historial","")}")
                    val message = jsonObj.get("message").toString()
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("token", prefs.getString("api_key", "")!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    private fun saveTareaDetalle() {
        if(prefs.contains("id_tarea_historial")){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else if(validate()){
                Toast.makeText(applicationContext, R.string.error_fields_required, Toast.LENGTH_LONG).show()
            }else{
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}tareas/historial/${prefs.getString("id_tarea_historial","")}/detalle"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        Log.d("estado","enviado")
                        prefs.edit().remove("id_tarea").commit()
                        prefs.edit().remove("id_punto_tarea_detalle").commit()
                        prefs.edit().remove("id_tarea_historial").commit()
                        prefs.edit().remove("id_punto_tarea_detalles").commit()

                        prefs.edit().remove("checks").commit()
                        prefs.edit().remove("imagenes").commit()
                        prefs.edit().remove("detalles").commit()
                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["id_tareas_detalle"] = prefs.getString("id_punto_tarea_detalles", "")
                        parameters["completados"] = prefs.getString("checks", "")
                        parameters["imagenes"] = prefs.getString("imagenes", "")
                        parameters["descripciones"] = prefs.getString("detalles", "")
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no id_tarea_historial")
        }

    }

}
