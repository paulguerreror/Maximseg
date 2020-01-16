package com.neobit.maximseg

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_punto_tareas.*
import kotlinx.android.synthetic.main.fragment_punto_tareas.rvPuntoTareas
import kotlinx.android.synthetic.main.fragment_punto_tareas.progressView
import kotlinx.android.synthetic.main.fragment_punto_tareas.contentView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.PuntoTareaAdapter
import com.neobit.maximseg.data.model.PuntoTarea
import kotlinx.android.synthetic.main.fragment_obligaciones.*
import org.json.JSONObject
import java.util.HashMap

class PuntoTareasFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val recyclerView = findViewById<RecyclerView>(R.id.rvPuntoTareas)
        //recyclerView.layoutManager = LinearLayoutManager(this@PuntoTareasFragment.context, LinearLayout.VERTICAL, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@PuntoTareasFragment.context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_punto_tareas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getTareasPunto()
    }

    private fun getTareasPunto() {
        if (!NetworkUtils.isConnected(this@PuntoTareasFragment.context!!)) {
            Toast.makeText(this@PuntoTareasFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val puntotareas = ArrayList<PuntoTarea>()
            val queue = Volley.newRequestQueue(this@PuntoTareasFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto","")}/guardias_tareas?supervisor=0"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("registros")
                    Log.d(URL,strResp)
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_punto_tarea = jsonInner.get("id_punto_tarea").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val hora_inicio = jsonInner.get("hora_inicio").toString()
                        val info = jsonInner.getInt("info")
                        var done = 0
                        if(jsonInner.has("completado")){
                            done = 1
                        }
                        Log.d("add","$id_punto_tarea,$nombre,$info,$hora_inicio")
                        if(info == 0){
                            puntotareas.add(PuntoTarea(id_punto_tarea,nombre,info,hora_inicio,done))
                        }
                    }
                    rvPuntoTareas.layoutManager = LinearLayoutManager(this@PuntoTareasFragment.context!!,LinearLayoutManager.VERTICAL,false)
                    val adapter = PuntoTareaAdapter(puntotareas)
                    rvPuntoTareas.adapter = adapter

                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@PuntoTareasFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@PuntoTareasFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

companion object {
    fun newInstance(): PuntoTareasFragment {
        return PuntoTareasFragment()
    }
}
}
