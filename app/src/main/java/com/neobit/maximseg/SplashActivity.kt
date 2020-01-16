package com.neobit.maximseg

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat.startActivity
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.neobit.maximseg.LoginActivity
import com.neobit.maximseg.MainActivity
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.ProductoAdapter
import com.neobit.maximseg.data.model.User
import com.neobit.maximseg.data.model.producto
import org.json.JSONObject
import java.util.HashMap
import kotlin.properties.Delegates

class SplashActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var inv: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        inv = "1"
        confirmInventario()
        if(prefs.contains("api_key"))
            sendInventario()

        Handler().postDelayed({
            val i = if (!prefs.contains("guardias")) {
                Intent(this@SplashActivity, LoginActivity::class.java)
            } else if(inv != "0" && prefs.contains("id_inventario")){
                Intent(this@SplashActivity, InventarioActivity::class.java)
            }else{
                Intent(this@SplashActivity, MainActivity::class.java)
            }
            startActivity(i)
            finish()
        }, 3000)
        if (prefs.getString("guardias", "") != "") {
            val usuario = Klaxon().parse<User>(prefs.getString("guardias", ""))!!
        }
    }

    fun sendInventario(){
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}inventario"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d(URL,strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val id_inventario = jsonObj.get("id").toString()
                    prefs.edit().putString("id_inventario",id_inventario).apply()
                    Log.d("id_inventario",id_inventario)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    //Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
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
                    parameters["id_punto"] = prefs.getString("id_punto", "")
                    parameters["id_turno"] = prefs.getString("id_turno", "")
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

     fun confirmInventario() {
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto", "")!!}"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val puntos = jsonObj.getJSONObject("puntos")
                    val jsonArray = puntos.getJSONArray("productos")
                    if(jsonArray.length() == 0){
                        inv = "0"
                        Toast.makeText(applicationContext, resources.getString(R.string.error_no_products), Toast.LENGTH_LONG).show()
                    }else
                        inv ="1"
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    //Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    //Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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