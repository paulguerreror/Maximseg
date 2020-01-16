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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.neobit.maximseg.data.model.TareaDetalle
import kotlinx.android.synthetic.main.activity_reporte_tarea_detalle.*



import java.io.ByteArrayOutputStream

class ReporteTareaDetalleActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    private var imgString  = ""

    private lateinit var prefs: SharedPreferences
    private lateinit var  id_punto_tarea_detalles: String
    private lateinit var checks: String
    private lateinit var imagenes: String
    private lateinit var detalles: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_tarea_detalle)
        val actionbar = supportActionBar
        actionbar!!.title= resources.getString(R.string.title_activity_reporte_parada_tareas)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        btnReporteTareaDetalle.setOnClickListener{
            sendReportTareaDetalle()
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
                    Toast.makeText(this@ReporteTareaDetalleActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@ReporteTareaDetalleActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
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

    fun sendReportTareaDetalle(){
        editArray()
        if(txtReporteTareaDetalle.text.toString() == "" || imgString == ""){
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
        }else{
            editArray()
            finish()
            startActivity(Intent(this, TareasActivity::class.java))
        }
    }

    fun editArray(){
        val id = prefs.getString("id_punto_tarea_detalle", "")
        var arrayId = prefs.getString("id_punto_tarea_detalles", "").split("|")
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
                ck = txtReporteTareaDetalle.text.toString()
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
        startActivity(Intent(this, TareasActivity::class.java))
    }


}
