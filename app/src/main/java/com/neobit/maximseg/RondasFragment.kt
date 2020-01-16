package com.neobit.maximseg

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.PuntoTareaAdapter
import com.neobit.maximseg.adapters.RondaAdapter
import com.neobit.maximseg.data.model.Ronda
import kotlinx.android.synthetic.main.activity_ronda.*
import kotlinx.android.synthetic.main.fragment_punto_tareas.*
import kotlinx.android.synthetic.main.fragment_rondas.*
import kotlinx.android.synthetic.main.fragment_rondas.contentView
import kotlinx.android.synthetic.main.fragment_rondas.progressView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

import org.json.JSONObject
import java.util.HashMap


class RondasFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLoc: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@RondasFragment.context)
        if(!prefs.contains("ronda_historial")){
            prefs.edit().remove("id_ronda").commit()
        }
        (activity as MainActivity).setBar("Rondas",false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rondas, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        currentLoc = LatLng(prefs.getString("lat", "")!!.toDouble(), prefs.getString("lon", "")!!.toDouble())
        mFusedLocationProviderClient = LocationServices.
            getFusedLocationProviderClient(this@RondasFragment.context!!)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        DisplayLayout()
        btnRonda.setOnClickListener{
            startRonda()
        }
        btnverRondas.setOnClickListener {
            Log.d("id_ronda",prefs.getString("id_ronda",""))
            if(prefs.contains("id_ronda")){
                startActivity(Intent(this@RondasFragment.context!!, RondaActivity::class.java))
            }
        }
        btnEndRound.setOnClickListener{
            val builder = AlertDialog.Builder(this@RondasFragment.context!!)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_end_round)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    DialogEndRonda()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            true
        }
        btnScan.setOnClickListener {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            (activity as MainActivity).getLocationUpdates()
            if(prefs.contains("ronda_historial")){
                run {
                    IntentIntegrator(this@RondasFragment.activity).initiateScan()
                }
            }else{
                Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.no_round), Toast.LENGTH_LONG).show()
            }
        }

    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun DisplayLayout(){
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if(prefs.contains("ronda_historial")){
            content_rondas.visibility = View.GONE
            card_ronda.visibility = View.VISIBLE
            btnRonda.visibility = View.GONE
            txtDateRound.visibility = View.VISIBLE
            txtTimeRound.visibility = View.VISIBLE
            btnScan.visibility = View.VISIBLE
            btnEndRound.visibility = View.VISIBLE
            val ronda_historial: JSONObject = JSONObject(prefs.getString("ronda_historial",""))
            val fecha_creacion = ronda_historial.get("fecha_creacion").toString().split(" ")
            txtDateRound.text = fecha_creacion[0]
            txtTimeRound.text = fecha_creacion[1]
            progressView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
        }else{
            getRondas()
            card_ronda.visibility = View.GONE
            txtDateRound.visibility = View.GONE
            txtTimeRound.visibility = View.GONE
            btnScan.visibility = View.GONE
            btnEndRound.visibility = View.GONE
            btnRonda.visibility = View.VISIBLE
            content_rondas.visibility = View.VISIBLE
        }
    }

    fun startRonda(){
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        if(prefs.contains("ronda_historial")){
            Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_round), Toast.LENGTH_LONG).show()
        }else{
            if (!NetworkUtils.isConnected(this@RondasFragment.context!!)) {
                Toast.makeText(this@RondasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
                val queue = Volley.newRequestQueue(this@RondasFragment.context!!)
                val URL = "${Utils.URL_SERVER}punto/${prefs.getString("id_punto","")}/rondas/create"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d(URL,strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val ronda = jsonObj.get("detalle").toString()
                        prefs.edit().putString("ronda_historial",ronda).apply()
                        DisplayLayout()
                        Toast.makeText(this@RondasFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
        DisplayLayout()
    }

    fun scanRonda(codigo:String){
        if (!NetworkUtils.isConnected(this@RondasFragment.context!!)) {
                Toast.makeText(this@RondasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
            val queue = Volley.newRequestQueue(this@RondasFragment.context!!)
                val ronda_historial = JSONObject(prefs.getString("ronda_historial",""))
                val id_ronda_historial = ronda_historial.get("id_ronda_historial").toString()
                val URL = "${Utils.URL_SERVER}rondas/$id_ronda_historial/paradas"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d(URL,strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val ronda = jsonObj.get("detalle").toString()
                        prefs.edit().putString("ronda",ronda).apply()
                        Toast.makeText(this@RondasFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                        if(jsonObj.getBoolean("completada")){
                            DialogEndRonda()
                            Toast.makeText(this@RondasFragment.context!!, jsonObj.get("Ronda completada!").toString(), Toast.LENGTH_LONG).show()
                        }
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        validateRonda()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["codigo"] = codigo
                        parameters["latitud"] = prefs.getString("lat", "")!!
                        parameters["longitud"] = prefs.getString("lon", "")!!
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
    }

    fun validateRonda(){
        if(prefs.contains("ronda")){
            val ronda = JSONObject(prefs.getString("ronda",""))
            val tareas = ronda.getBoolean("tareas")
            if(tareas){
                Log.d("tareas","si")
                startActivity(Intent(this@RondasFragment.context!!, ParadaTareasActivity::class.java))
            }else{
                Log.e("tareas","no")
            }
        }else{
            Log.e("ronda","no existe")
        }
    }

    fun endRonda(descripcion:String){

        if(prefs.contains("ronda_historial")){
            if (!NetworkUtils.isConnected(this@RondasFragment.context!!)) {
                Toast.makeText(this@RondasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {

                val ronda_historial = JSONObject(prefs.getString("ronda_historial",""))
                val id_ronda_historial = ronda_historial.get("id_ronda_historial").toString()
                val queue = Volley.newRequestQueue(this@RondasFragment.context!!)
                val URL = "${Utils.URL_SERVER}rondas/$id_ronda_historial/finish"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        prefs.edit().remove("ronda_historial").commit()
                        prefs.edit().remove("ronda_historial").commit()
                        DisplayLayout()
                        Toast.makeText(this@RondasFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this@RondasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("token", prefs.getString("api_key", "")!!)
                        return headers
                    }
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()
                        parameters["descripcion"] = descripcion
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_round), Toast.LENGTH_LONG).show()
        }
        DisplayLayout()
    }

    fun DialogEndRonda() {
        val builder = AlertDialog.Builder(this@RondasFragment.context!!)
        builder.setTitle("Finalizar Ronda").setMessage(R.string.alert_description)
        val view = layoutInflater.inflate(R.layout.dialog_end_ronda, null)
        val txtEndRonda = view.findViewById(R.id.txtEndRonda) as EditText
        builder.setView(view)
        builder.setPositiveButton(android.R.string.yes) { dialog, p1 ->
            val descripcion = txtEndRonda.text.toString()
            endRonda(descripcion)
            txtEndRonda.error = getString(R.string.error_dialog)
            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents != null){
                Log.d("qr",result.contents)
                scanRonda(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun getRondas() {
        if (!NetworkUtils.isConnected(this@RondasFragment.context!!)) {
            Toast.makeText(this@RondasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val rondas = ArrayList<Ronda>()
            val queue = Volley.newRequestQueue(this@RondasFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto","")}/rondas"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("rondas")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_ronda = jsonInner.get("id_ronda").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        Log.d("add","$id_ronda,$nombre")
                        rondas.add(Ronda(id_ronda,nombre))
                    }
                    rvRondas.layoutManager = LinearLayoutManager(this@RondasFragment.context!!,LinearLayoutManager.VERTICAL,false)
                    val adapter = RondaAdapter(rondas)
                    rvRondas.adapter = adapter

                    rvRondas.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@RondasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@RondasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
