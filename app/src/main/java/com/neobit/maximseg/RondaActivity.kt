package com.neobit.maximseg

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.webkit.WebViewFragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.RondaParadaAdapter
import com.neobit.maximseg.data.model.RondaParada
import kotlinx.android.synthetic.main.activity_ronda.*
import kotlinx.android.synthetic.main.activity_ronda.contentView
import kotlinx.android.synthetic.main.activity_ronda.progressView
import com.neobit.maximseg.MainActivity

import org.json.JSONObject
import java.util.HashMap

class RondaActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ronda)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val actionbar = supportActionBar
        actionbar!!.title= resources.getString(R.string.title_paradas)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if(!prefs.contains("id_ronda")){
            finish()
        }
        getParadas()
        if(prefs.contains("ronda_historial")){
            btnStartRonda.visibility = View.GONE
        }
        btnStartRonda.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_start_round)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    startRonda()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    fun startRonda(){
        if(prefs.contains("ronda_historial")){
            Toast.makeText(this, resources.getString(R.string.error_round), Toast.LENGTH_LONG).show()
        }else{
            if (!NetworkUtils.isConnected(this)) {
                Toast.makeText(this, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(this)
                val URL = "${Utils.URL_SERVER}punto/${prefs.getString("id_punto","")}/rondas/create"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val ronda = jsonObj.get("detalle").toString()
                        prefs.edit().putString("ronda_historial",ronda).apply()
                        Log.d("ronda_historial",ronda)
                        Toast.makeText(this, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                        finish()
                        startActivity(Intent(this, MainActivity::class.java))

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["id_ronda"] = prefs.getString("id_ronda", "")
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }
    }

    private fun getParadas() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val rondasparadas = ArrayList<RondaParada>()
            val queue = Volley.newRequestQueue(this)
            val URL = "${Utils.URL_SERVER}rondas/${prefs.getString("id_ronda","")}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)

                    val jsonObj: JSONObject = JSONObject(strResp)
                    val ronda = jsonObj.getJSONObject("rondas")
                    txtRondaNombre.text = ronda.getString("nombre")
                    val jsonArray = ronda.getJSONArray("paradas")

                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_ronda_parada = jsonInner.get("id_ronda_parada").toString().toInt()
                        val id_ronda = jsonInner.get("id_ronda").toString().toInt()
                        val tareas = jsonInner.getBoolean("tareas")
                        val codigo = jsonInner.getJSONObject("parada").get("codigo").toString()
                        val nombre = jsonInner.getJSONObject("parada").get("nombre").toString()
                        val hora = jsonInner.get("hora").toString()

                        Log.d("add","$id_ronda_parada,$id_ronda,$tareas,$codigo,$nombre,$hora")
                        rondasparadas.add(RondaParada(id_ronda_parada,id_ronda,tareas,codigo,nombre,hora))
                    }

                    rvRondaParada.layoutManager = LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = RondaParadaAdapter(rondasparadas)
                    rvRondaParada.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
}
