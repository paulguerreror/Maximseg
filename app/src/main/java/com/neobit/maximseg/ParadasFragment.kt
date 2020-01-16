package com.neobit.maximseg

import android.content.Context
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
import android.widget.Toast
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
import org.json.JSONObject
import java.util.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


class ParadasFragment : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paradas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this@ParadasFragment.context!!)
        (activity as MainActivity).setBar("Paradas",false)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getParadas()
        if(prefs.contains("ronda_historial")){
            btnStartRonda.visibility = View.GONE
        }
        btnStartRonda.setOnClickListener {
            startRonda()
        }
    }

    fun startRonda(){
        if(prefs.contains("ronda_historial")){
            Toast.makeText(this@ParadasFragment.context!!, resources.getString(R.string.error_round), Toast.LENGTH_LONG).show()
        }else{
            if (!NetworkUtils.isConnected(this@ParadasFragment.context!!)) {
                Toast.makeText(this@ParadasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
                val queue = Volley.newRequestQueue(this@ParadasFragment.context!!)
                val URL = "${Utils.URL_SERVER}punto/${prefs.getString("id_punto","")}/rondas/create"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val ronda = jsonObj.get("detalle").toString()
                        prefs.edit().putString("ronda_historial",ronda).apply()
                        Log.d("ronda_historial",ronda)
                        Toast.makeText(this@ParadasFragment.context!!, jsonObj.get("message").toString(), Toast.LENGTH_LONG).show()
                        (activity as MainActivity).onBackPressed()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(this@ParadasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Toast.makeText(this@ParadasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
        if (!NetworkUtils.isConnected(this@ParadasFragment.context!!)) {
            Toast.makeText(this@ParadasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val rondasparadas = ArrayList<RondaParada>()
            val queue = Volley.newRequestQueue(this@ParadasFragment.context!!)
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

                    rvRondaParada.layoutManager = LinearLayoutManager(this@ParadasFragment.context!!,
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
                    Toast.makeText(this@ParadasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ParadasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ParadasFragment().apply {

            }
    }
}
