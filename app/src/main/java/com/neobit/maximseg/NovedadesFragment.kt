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
import com.neobit.maximseg.adapters.NovedadAdapter
import com.neobit.maximseg.data.model.Novedad
import kotlinx.android.synthetic.main.fragment_novedades.*
import kotlinx.android.synthetic.main.fragment_obligaciones.contentView
import kotlinx.android.synthetic.main.fragment_obligaciones.progressView
import org.json.JSONObject
import java.util.HashMap

class NovedadesFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setBar("Novedades Públicas",false)
        (activity as MainActivity).hidefab(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_novedades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        progressView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
        prefs.edit().remove("id_novedad").commit()
        getNovedades()
        btnNovedad.setOnClickListener {
            //startActivity(Intent(this@NovedadesFragment.context, NovedadActivity::class.java))
            prefs.edit().putString("cliente","1").apply()
            Log.d("cliente",prefs.getString("cliente",""))
            val fragment = NovedadCrearFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragmentManager!!.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left).replace(R.id.frame_container, fragment).commit()
        }
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    private fun getNovedades() {
        if (!NetworkUtils.isConnected(this@NovedadesFragment.context!!)) {
            Toast.makeText(this@NovedadesFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val novedades = ArrayList<Novedad>()
            val queue = Volley.newRequestQueue(this@NovedadesFragment.context!!)
            val ext = "novedades?cliente=1"

            val URL = "${Utils.URL_SERVER}punto/${prefs.getString("id_punto","")}/novedades?cliente=1"
            val stringRequest = object : StringRequest(Method.GET, URL, Response.Listener<String> { response ->
                try {
                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    Log.d("$ext",strResp)

                    val jsonArray = jsonObj.getJSONArray("novedades")

                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        val id_novedad = jsonInner.get("id_novedad").toString().toInt()
                        val tipo = jsonInner.get("tipo").toString()
                        val descripcion = jsonInner.get("descripcion").toString()
                        val imagen = jsonInner.get("imagen").toString()
                        val fecha_creacion = jsonInner.get("fecha_creacion").toString()
                        val cliente = jsonInner.getInt("cliente")

                        val creador =jsonInner.getJSONObject("creador").get("nombres").toString()

                        Log.d("add","$id_novedad,$descripcion,$creador,$tipo,$imagen,$cliente,$fecha_creacion")
                        novedades.add(Novedad(id_novedad,creador,tipo,descripcion,imagen,cliente,fecha_creacion))
                    }
                    rvNovedades.layoutManager = LinearLayoutManager(this@NovedadesFragment.context!!,
                        LinearLayoutManager.VERTICAL,false)
                    val adapter = NovedadAdapter(novedades)
                    rvNovedades.adapter = adapter
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
                    Toast.makeText(this@NovedadesFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    Toast.makeText(this@NovedadesFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NovedadesFragment().apply {

            }
    }
}
