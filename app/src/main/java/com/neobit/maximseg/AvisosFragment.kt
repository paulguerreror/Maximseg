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
import com.neobit.maximseg.data.model.Aviso
import com.neobit.maximseg.R
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import com.neobit.maximseg.adapters.AvisoAdapter
import kotlinx.android.synthetic.main.fragment_avisos.*
import org.json.JSONObject
import java.util.HashMap
import java.text.SimpleDateFormat
import java.util.Calendar


class AvisosFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var prefs: SharedPreferences

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).setBar("Avisos",false)
        return inflater.inflate(R.layout.fragment_avisos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.visibility = View.GONE
        progressView.visibility = View.VISIBLE

        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        getAvisos()
    }

    private fun getAvisos() {
        if (!NetworkUtils.isConnected(this@AvisosFragment.context!!)) {
            Toast.makeText(this@AvisosFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val avisos = ArrayList<Aviso>()
            val queue = Volley.newRequestQueue(this@AvisosFragment.context!!)

            val URL = "${Utils.URL_SERVER}avisos"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$URL",strResp)

                    val jsonArray = jsonObj.getJSONArray("registros")
                    if(jsonArray.length() != 0){
                        txtPlain.visibility = View.GONE
                        rvAvisos.visibility = View.VISIBLE
                        for (i in 0 until jsonArray.length()) {
                            var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                            val id_aviso_historial = jsonInner.get("id_aviso_historial").toString().toInt()
                            val validado = jsonInner.getInt("validado")
                            val id_aviso = jsonInner.getInt("id_aviso")
                            val aviso = jsonInner.getJSONObject("aviso")
                            val hora = aviso.get("hora").toString()
                            val descripcion = aviso.get("descripcion").toString()
                            Log.d("add","$id_aviso_historial,$validado,$id_aviso,$hora,$descripcion")
                            avisos.add(Aviso(id_aviso_historial,validado,id_aviso,hora,descripcion))
                        }
                        rvAvisos.layoutManager = LinearLayoutManager(this@AvisosFragment.context!!,
                            LinearLayoutManager.VERTICAL,false)
                        val adapter = AvisoAdapter(avisos)
                        rvAvisos.adapter = adapter
                    }
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
                    Toast.makeText(this@AvisosFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@AvisosFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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



    // TODO: Rename method, update argument and hook method into UI event
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

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AvisosFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
