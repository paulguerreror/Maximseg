package com.neobit.maximseg

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.NovedadDetalleAdapter
import com.neobit.maximseg.adapters.RondaTareaAdapter
import com.neobit.maximseg.data.model.NovedadDetalle
import com.neobit.maximseg.data.model.RondaTarea
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_novedad_detalle.*
import kotlinx.android.synthetic.main.activity_novedad_detalles.*
import kotlinx.android.synthetic.main.activity_novedad_detalles.contentView
import kotlinx.android.synthetic.main.activity_novedad_detalles.progressView
import org.json.JSONObject
import java.util.HashMap

class NovedadDetallesActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novedad_detalles)
        val actionbar = supportActionBar
        actionbar!!.title= resources.getString(R.string.title_activity_novedad_detalles)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getNovedadById()

        btnNovedadDetalle.setOnClickListener {
            finish()
            startActivity(Intent(this, NovedadDetalleActivity::class.java))
        }
        cardprivacidad.setOnClickListener {
            val cliente =cardprivacidad.getTag().toString()
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_change_privacy)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    changeNovedadPrivacy(cliente.toInt())
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

    private fun changeNovedadPrivacy(cliente: Int) {
        var client = 0
        if(cliente == 0){
            client = 1
        }
        if(prefs.contains("id_novedad")){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
                val stringRequest = object : StringRequest(Method.PUT, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                         getNovedadById()

                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
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
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["cliente"] = client.toString()
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no novedad")
        }

    }


    fun alertNovedadDetallePrivacidad(view: View){
        Log.d("data","hola")
        //val data  = view.getTag(R.id.id).toString()
        var cliente =view.getTag(R.id.cliente).toString().toInt()
        if(cliente==1)
            cliente = 0
        else
            cliente = 1
        val id = view.getTag(R.id.id).toString().toInt()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_change_privacy)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                dialog.cancel()
                changeNovedadDetallePrivacy(cliente,id)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
        true
    }

    private fun changeNovedadDetallePrivacy(cliente: Int,id:Int) {
        if(prefs.contains("id_novedad")){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}novedades/detalle/$id"
                val stringRequest = object : StringRequest(Method.PUT, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        getNovedadById()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
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
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["cliente"] = cliente.toString()
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no novedad")
        }

    }

    override fun onBackPressed() {
        prefs.edit().remove("id_novedad").commit()
        finish()
    }

    private fun getNovedadById() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvNovedadDetalles)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val novedaddetalle = ArrayList<NovedadDetalle>()
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val novedad = jsonObj.getJSONObject("novedades")
                    val detalle = novedad.getJSONArray("detalle")
                    cardprivacidad.setTag(novedad.getInt("cliente"))
                    txtDetalleFecha.text = novedad.getString("fecha_creacion")
                    if(novedad.getString("imagen") == ""){
                        imgDetalleDescripcion.visibility = View.GONE
                    }else{
                        Picasso.get().load(Utils.URL_MEDIA + novedad.getString("imagen")).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(imgDetalleDescripcion)
                    }
                    txtDetalleDescripcion.text = novedad.getString("descripcion")
                    txtDetalleNombre.text = novedad.getJSONObject("creador").getString("nombres")
                    if(novedad.getInt("cliente") == 1){
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#26dad2"))
                        txtprivacidad.text = "Público"
                    }else{
                        cardprivacidad.setCardBackgroundColor(Color.parseColor("#ef5350"))
                        txtprivacidad.text = "Privado"
                    }
                    for (i in 0 until detalle.length()) {
                        var jsonInner: JSONObject = detalle.getJSONObject(i)
                        val id_novedad_detalle = jsonInner.get("id_novedad_detalle").toString().toInt()
                        val nombres = jsonInner.getJSONObject("id_creador").getString("nombres")
                        val tipo = jsonInner.get("tipo").toString()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val imagen = jsonInner.get("imagen").toString()
                        val fecha = jsonInner.get("fecha_creacion").toString()
                        val cliente = jsonInner.getInt("cliente")

                        Log.d("add","$id_novedad_detalle|$cliente|$nombres|$tipo|$descripcion|$imagen|$fecha|")
                        novedaddetalle.add(NovedadDetalle(id_novedad_detalle,cliente,nombres,tipo,descripcion,imagen,fecha))
                    }
                    val adapter = NovedadDetalleAdapter(novedaddetalle)
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

}
