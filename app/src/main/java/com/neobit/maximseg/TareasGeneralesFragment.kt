package com.neobit.maximseg

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.lang.UCharacter.getAge
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.data.model.User
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.PuntoTareaAdapter
import com.neobit.maximseg.data.model.PuntoTarea
import kotlinx.android.synthetic.main.fragment_obligaciones.*
import kotlinx.android.synthetic.main.fragment_tareas_generales.*
import kotlinx.android.synthetic.main.fragment_tareas_generales.progressView
import kotlinx.android.synthetic.main.fragment_tareas_generales.contentView

import kotlinx.android.synthetic.main.fragment_tareas_generales.rvGeneralTareas
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_punto_tareas.*
import org.json.JSONObject
import java.util.HashMap

class TareasGeneralesFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_tareas_generales, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getTareaGenerales()

    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    private fun getTareaGenerales() {
        if (!NetworkUtils.isConnected(this@TareasGeneralesFragment.context!!)) {
            Toast.makeText(this@TareasGeneralesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val puntotareas = ArrayList<PuntoTarea>()
            val queue = Volley.newRequestQueue(this@TareasGeneralesFragment.context!!)
            val URL = "${Utils.URL_SERVER}puntos/${prefs.getString("id_punto","")}/tareas?supervisor=0"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    Log.d("$URL",strResp)
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray = jsonObj.getJSONArray("tareas")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_punto_tarea = jsonInner.get("id_punto_tarea").toString().toInt()
                        val nombre = jsonInner.get("nombre").toString()
                        val hora_inicio = jsonInner.get("hora_inicio").toString()
                        val info = jsonInner.getInt("info")
                        Log.d("add","$id_punto_tarea,$nombre,$info,$hora_inicio")
                        if(info == 1){
                            puntotareas.add(PuntoTarea(id_punto_tarea,nombre,info,hora_inicio,0))
                        }
                    }
                    rvGeneralTareas.layoutManager = LinearLayoutManager(this@TareasGeneralesFragment.context!!,LinearLayoutManager.VERTICAL,false)
                    val adapter = PuntoTareaAdapter(puntotareas)
                    rvGeneralTareas.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(this@TareasGeneralesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this@TareasGeneralesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(): TareasGeneralesFragment {
            return TareasGeneralesFragment()
        }
    }
}
