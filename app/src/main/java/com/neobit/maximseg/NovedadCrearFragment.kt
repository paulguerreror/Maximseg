package com.neobit.maximseg

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.neobit.maximseg.Utils.NetworkUtils
import com.neobit.maximseg.Utils.Utils
import kotlinx.android.synthetic.main.fragment_novedad_crear.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

class NovedadCrearFragment : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_novedad_crear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        if(prefs.contains("cliente")){
            if(prefs.getString("cliente","") == "1"){
                (activity as MainActivity).setBar("Crear Novedad Pública",false)
            }else{
                (activity as MainActivity).setBar("Crear Novedad Privada",false)
            }
        }else{
            prefs.edit().putString("cliente","").apply()
        }

        (activity as MainActivity).hidefab(true)

        /*
         progressView.visibility = View.VISIBLE
         contentView.visibility = View.GONE

         */
        btnNovedad.setOnClickListener {
            createNovedad()
        }
        btnAdjunto.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    fun dispatchTakePictureIntent() {
        Dexter.withActivity(this@NovedadCrearFragment.activity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(this@NovedadCrearFragment.context!!.packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@NovedadCrearFragment.context, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@NovedadCrearFragment.context, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
            .onSameThread()
            .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null) {
                    val imageBitmap = data.extras.get("data") as Bitmap
                    val baos = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    imgString = Base64.encodeToString(b, Base64.DEFAULT)
                    btnAdjunto.setBackgroundResource(R.drawable.adjuntar_done)
                    txtadjunto.setText(R.string.btnAdjuntado)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }

    private fun createNovedad() {
        if(txtCreateNovedad.text.toString() != "" ){
            if (!NetworkUtils.isConnected(this@NovedadCrearFragment.context!!)) {
                Toast.makeText(this@NovedadCrearFragment.context!!, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(this@NovedadCrearFragment.context!!)
                val URL = "${Utils.URL_SERVER}novedades"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadCrearFragment.context!!, message, Toast.LENGTH_LONG).show()
                        (activity as MainActivity).onBackPressed()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        e.printStackTrace()
                        Toast.makeText(this@NovedadCrearFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        error.printStackTrace()
                        Toast.makeText(this@NovedadCrearFragment.context!!, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(this@NovedadCrearFragment.context!!, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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
                        parameters["cliente"] = prefs.getString("cliente", "")
                        parameters["descripcion"] = txtCreateNovedad.text.toString()
                        parameters["imagen"] = imgString
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Toast.makeText(this@NovedadCrearFragment.context!!, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        }
    }
/*
    fun onBackPressed() {
        Log.d("back","backed")
        val fragment = NovedadesFragment()
        val bundle = Bundle()
        fragment.arguments = bundle
        fragmentManager!!.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }*/

    override fun onDetach() {
        (activity as MainActivity).hidefab(false)
        prefs.edit().remove("cliente").commit()
        super.onDetach()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NovedadCrearFragment().apply {

            }
    }
}
