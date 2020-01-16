package com.neobit.maximseg

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.content_reporte_parada_tarea.txtReporteParadaTarea
import kotlinx.android.synthetic.main.content_reporte_parada_tarea.btnReporteParadaTarea

import kotlinx.android.synthetic.main.activity_reporte.*
import kotlinx.android.synthetic.main.content_reporte_parada_tarea.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.HashMap

class ReporteParadaTareaActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""

    private lateinit var prefs: SharedPreferences
    private lateinit var  id_ronda_tareas: String
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var detalles: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_parada_tareas)
        setSupportActionBar(toolbar)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        btnReporteParadaTarea.setOnClickListener{
            sendReportParadaTarea()
        }
    }

    fun dispatchTakePictureIntent(view: View) {
        Dexter.withActivity(this@ReporteParadaTareaActivity)
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
                    Toast.makeText(this@ReporteParadaTareaActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@ReporteParadaTareaActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
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
                    //btnAdjunto.setBackgroundResource(R.color.colorPrimary)
                    btnAdjunto.setBackgroundResource(R.drawable.adjuntar_done)

                    val txtadjunto = findViewById(R.id.txtadjunto) as TextView
                    txtadjunto.setText(R.string.btnAdjuntado)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }

    fun sendReportParadaTarea(){
        editArray()
        if(txtReporteParadaTarea.text.toString() == "" || imgString == ""){
            Toast.makeText(this@ReporteParadaTareaActivity, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        }else{
            editArray()
            finish()
            startActivity(Intent(this@ReporteParadaTareaActivity, ParadaTareasActivity::class.java))

        }
    }

    fun editArray(){
        val id = prefs.getString("id_ronda_tarea", "")
        var arrayId = prefs.getString("id_ronda_tareas", "").split("|")
        val Listchecks = prefs.getString("checks", "").split("|")
        val Arraychecks = arrayListOf<String>()
        val Listimagenes = prefs.getString("imagenes", "").split("|")
        val Arrayimagenes = arrayListOf<String>()
        val Listdetalles = prefs.getString("detalles", "").split("|")
        val Arraydetalles = arrayListOf<String>()
        val position = arrayId.indexOf(id)
        var i= 0
        Listchecks.forEach{
            var ck = it
            if(i == position){
                    ck = "0"
            }
            Arraychecks.add(ck)
            i++
        }
        checks = Arraychecks.toString().replace("[","").replace("]","").replace(" ","").replace(",","|")
        prefs.edit().putString("checks",checks).apply()
        //imagenes
        i=0
        Listimagenes.forEach{
            var ck = it
            if(i == position){
                ck = imgString
            }
            Arrayimagenes.add(ck)
            i++
        }
        imagenes = Arrayimagenes.toString().replace("[","").replace("]","").replace(" ","").replace(",","|")
        prefs.edit().putString("imagenes",imagenes).apply()
        //detalles
        i=0
        Listdetalles.forEach{
            var ck = it
            if(i == position){
                ck = txtReporteParadaTarea.text.toString()
            }
            Arraydetalles.add(ck)
            i++
        }
        detalles = Arraydetalles.toString().replace("[","").replace("]","").replace("  "," ").replace(",","|")
        prefs.edit().putString("detalles",detalles).apply()

        Log.d("report_checks",prefs.getString("checks", ""))
        Log.d("report_imagenes",prefs.getString("imagenes", ""))
        Log.d("report_detalles",prefs.getString("detalles", ""))
    }

    override fun onBackPressed() {
        finish()
        startActivity(Intent(this@ReporteParadaTareaActivity, ParadaTareasActivity::class.java))
    }



}
