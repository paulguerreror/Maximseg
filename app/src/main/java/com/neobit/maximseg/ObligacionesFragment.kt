package com.neobit.maximseg

import android.content.Context
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
import com.neobit.maximseg.adapters.TareaAdapter
import com.neobit.maximseg.data.model.Tarea
import kotlinx.android.synthetic.main.fragment_obligaciones.*
import kotlinx.android.synthetic.main.fragment_obligaciones.txtVision
import kotlinx.android.synthetic.main.fragment_obligaciones.progressView
import kotlinx.android.synthetic.main.fragment_obligaciones.contentView


import org.json.JSONObject
import java.util.HashMap

class ObligacionesFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setBar("Obligaciones",false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        getTareaGenerales()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_obligaciones, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }


    private fun getTareaGenerales() {
        if (!NetworkUtils.isConnected(this@ObligacionesFragment.context!!)) {
            Toast.makeText(this@ObligacionesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val tareas = ArrayList<Tarea>()
            val queue = Volley.newRequestQueue(this@ObligacionesFragment.context!!)
            val URL = "${Utils.URL_SERVER}tareas"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)
                    val jsonArray = jsonObj.getJSONArray("tareas")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_tarea = jsonInner.get("id_tarea").toString().toInt()
                        val descripcion = jsonInner.get("descripcion").toString()
                        Log.d("add","$id_tarea,$descripcion")
                        tareas.add(Tarea(id_tarea,descripcion))
                    }
                    rvObligaciones.layoutManager = LinearLayoutManager(this@ObligacionesFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = TareaAdapter(tareas)
                    rvObligaciones.adapter = adapter
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ObligacionesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ObligacionesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@ObligacionesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ObligacionesFragment().apply {
            }
    }
}
