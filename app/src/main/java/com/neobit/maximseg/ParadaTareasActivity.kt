package com.neobit.maximseg

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.RondaTareaAdapter
import com.neobit.maximseg.data.model.RondaTarea
import kotlinx.android.synthetic.main.content_parada_tareas.btnParadaTareas
import kotlinx.android.synthetic.main.content_parada_tareas.contentView
import kotlinx.android.synthetic.main.content_parada_tareas.progressView
import kotlinx.android.synthetic.main.fragment_obligaciones.*

import org.json.JSONObject
import java.util.HashMap

class ParadaTareasActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var  id_ronda_tareas: String
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var detalles: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parada_tareas)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if(prefs.contains("id_ronda_tareas")){
            Log.d("existe",prefs.getString("id_ronda_tareas",""))
            getTareasByParada2()
        }else{
            getTareasByParada()
            Log.d("existe","no existe")
        }
        btnParadaTareas.setOnClickListener {
            sendReportParadaTarea()
        }
    }

    private fun getTareasByParada() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvParadaTareas)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val rondatareas = ArrayList<RondaTarea>()
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val ronda = JSONObject(prefs.getString("ronda",""))
            val id_ronda_parada  = ronda.get("id_ronda_parada").toString()
            val URL = "${Utils.URL_SERVER}paradas/$id_ronda_parada/tareas"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_ronda_tareas = ""
                    checks= ""
                    imagenes= ""
                    detalles= ""
                    var strResp = response.toString()
                    Log.d("$URL",strResp)

                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("tareas")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_ronda_tarea = jsonInner.get("id_ronda_tarea").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        Log.d("add","$id_ronda_tarea|$nombre|$nombre")
                        rondatareas.add(
                            RondaTarea(
                                id_ronda_tarea,
                                nombre,
                                "%"
                            )
                        )
                        if(id_ronda_tareas == ""){
                            id_ronda_tareas +=  id_ronda_tarea.toString()
                            checks +=  "%"
                        }else{
                            id_ronda_tareas += "|" + jsonInner.get("id_ronda_tarea").toString()
                            checks +=  "|%"
                            imagenes += "|"
                            detalles += "|"
                        }
                        Log.d("id_ronda_tareas",id_ronda_tareas)
                    }
                    val adapter = RondaTareaAdapter(rondatareas)
                    recyclerView.adapter = adapter
                    prefs.edit().putString("id_ronda_tareas",id_ronda_tareas).apply()
                    prefs.edit().putString("checks",checks).apply()
                    prefs.edit().putString("imagenes",imagenes).apply()
                    prefs.edit().putString("detalles",detalles).apply()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
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

    private fun getTareasByParada2(){
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvParadaTareas)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val rondatareas = ArrayList<RondaTarea>()
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val ronda = JSONObject(prefs.getString("ronda",""))
            val id_ronda_parada  = ronda.get("id_ronda_parada").toString()
            val URL = "${Utils.URL_SERVER}paradas/$id_ronda_parada/tareas"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    id_ronda_tareas = ""
                    var strResp = response.toString()
                    Log.d("URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("tareas")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_ronda_tarea = jsonInner.get("id_ronda_tarea").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val checked = getCheck(i)
                        Log.d("add2","$id_ronda_tarea|$nombre|$checked")
                        rondatareas.add(
                            RondaTarea(
                                id_ronda_tarea,
                                nombre,
                                checked
                            )
                        )
                    }

                    val adapter = RondaTareaAdapter(rondatareas)
                    recyclerView.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
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

    fun checkRondaTarea(view: View){
        val id = view.getTag().toString()
        var arrayId = prefs.getString("id_ronda_tareas", "").split("|")
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
        checks = Arraychecks.toString().replace("[","").replace("]","").replace("  "," ").replace(", ","|")
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
        Log.d("id_ronda_tareas",prefs.getString("id_ronda_tareas", ""))
        Log.d("checks",prefs.getString("checks", ""))
        Log.d("imagenes",prefs.getString("imagenes", ""))
        Log.d("detalles",prefs.getString("detalles", ""))
    }

    fun reportRondaTarea(view: View){
        prefs.edit().putString("id_ronda_tarea", view.getTag().toString()).apply()
        finish()
        startActivity(Intent(this@ParadaTareasActivity, ReporteParadaTareaActivity::class.java))
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

    private fun sendReportParadaTarea() {
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else if(validate()){
            Toast.makeText(applicationContext, R.string.error_fields_required, Toast.LENGTH_LONG).show()
        }else{
            val ronda = JSONObject(prefs.getString("ronda",""))
            val id_ronda  = ronda.get("id_ronda").toString()
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}paradas/$id_ronda/tareas/batch"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    Log.d("estado","enviado")
                    prefs.edit().remove("id_ronda_tareas").commit()
                    prefs.edit().remove("ronda").commit()
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
                    parameters["id_rondas_tareas"] = prefs.getString("id_ronda_tareas", "")
                    parameters["completados"] = prefs.getString("checks", "")
                    parameters["imagenes"] = prefs.getString("imagenes", "")
                    parameters["descripciones"] = prefs.getString("detalles", "")
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    override fun onBackPressed() {
        prefs.edit().remove("id_ronda_tareas").commit()
        prefs.edit().remove("ronda").commit()
        prefs.edit().remove("checks").commit()
        prefs.edit().remove("imagenes").commit()
        prefs.edit().remove("detalles").commit()
        finish()
    }


}
