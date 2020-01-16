package com.neobit.maximseg

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_novedad_detalle.*
import kotlinx.android.synthetic.main.activity_novedad_detalle.contentView
import kotlinx.android.synthetic.main.activity_novedad_detalle.progressView
import kotlinx.android.synthetic.main.activity_novedad_detalle.txtCreateNovedadDetalle
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.HashMap

class NovedadDetalleActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novedad_detalle)
        val actionbar = supportActionBar
        actionbar!!.title= resources.getString(R.string.title_activity_novedad)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        btnCreateNovedadDetalle.setOnClickListener {
            createNovedadDetalle()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        prefs.edit().remove("id_novedad").commit()
        finish()
    }

    fun dispatchTakePictureIntent(view: View) {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }
                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@NovedadDetalleActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@NovedadDetalleActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
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
                    val btnAdjunto = findViewById(R.id.btnAdjunto) as ImageButton
                    btnAdjunto.setBackgroundResource(R.drawable.adjuntar_done)

                    val txtadjunto = findViewById(R.id.txtadjunto) as TextView
                    txtadjunto.setText(R.string.btnAdjuntado)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }



    private fun createNovedadDetalle() {
        if(txtCreateNovedadDetalle.text.toString() != "" ){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                progressView.visibility = View.VISIBLE
                contentView.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}novedades/${prefs.getString("id_novedad","")}"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        var strResp = response.toString()
                        Log.d("$URL",strResp)
                        val jsonObj: JSONObject = JSONObject(strResp)
                        val message = jsonObj.get("message").toString()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        finish()
                        startActivity(Intent(this, NovedadDetallesActivity::class.java))

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
                        parameters["descripcion"] = txtCreateNovedadDetalle.text.toString()
                        parameters["imagen"] = imgString
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
        }else{
            Log.e("error","no id_tarea_historial")
        }
    }
}
