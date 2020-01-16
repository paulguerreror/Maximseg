package com.neobit.maximseg

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity;
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.graphics.Bitmap
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import android.util.Base64
import android.widget.EditText
import android.widget.LinearLayout
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import kotlinx.android.synthetic.main.app_bar_main.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken

import com.neobit.maximseg.Utils.NetworkUtils
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.neobit.maximseg.Utils.Utils.Companion.URL_SERVER
import com.neobit.maximseg.adapters.GuardiaAdapter
import com.neobit.maximseg.data.model.*
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import java.io.ByteArrayOutputStream

import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1356
    companion object {
        private var sharedInstance: LoginActivity? = null
        fun instance(): LoginActivity? {
            return sharedInstance
        }
    }
    private lateinit var prefs: SharedPreferences
    var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        fusedLocationClient = LocationServices.
            getFusedLocationProviderClient(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        getLocation()
        LoadGuardias()
        getToken()
        btnLoginFoto.setOnClickListener {
            dispatchTakePictureIntent()

        }

    }

    fun alertEditText(imagen: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Iniciar Turno").setMessage(R.string.alert_login)
        val view = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val txtAlert = view.findViewById(R.id.etprofile) as EditText
        builder.setView(view)
        builder.setPositiveButton(android.R.string.yes) { dialog, p1 ->
            val descripcion = txtAlert.text.toString()
            authUser(imagen,descripcion)
            dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    fun getLocation(){
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this,
                    {location : Location? ->
                        if(location == null) {
                            Log.e("error","didnt get Loc")
                        } else location.apply {
                            Log.d("coordenadas",location.toString())
                            Log.d("lat",location.latitude.toString())
                            Log.d("lon",location.longitude.toString())
                            prefs.edit().putString("lat", location.latitude.toString()).apply()
                            prefs.edit().putString("lon", location.longitude.toString()).apply()
                        }
                    })
        }
    }

    fun getToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("tk", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result?.token
                Log.d("firebase", token)
                prefs.edit().putString("token",token).apply()
            })
    }

    fun LoadGuardias(){
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvguardias)
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val guardias = ArrayList<Guardia>()
        if(prefs.contains("IdArrays")){
            Log.d("IdArrays",prefs.getString("IdArrays", ""))
            var IdArrays = prefs.getString("IdArrays", "").split(",")
            IdArrays.forEach{
                var guardia = prefs.getString("guardia$it","").split(",")
                Log.d("sending$it",prefs.getString("guardia$it", ""))

                guardias.add(Guardia(it.toInt(),guardia[0],guardia[1],guardia[2],guardia[3]))
            }
            val adapter = GuardiaAdapter(guardias)
            recyclerView.adapter = adapter
        }
    }

    fun dispatchTakePictureIntent() {
        getLocation()
        Dexter.withActivity(this@LoginActivity)
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
                    Toast.makeText(this@LoginActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(this@LoginActivity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
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
                    val imgString = Base64.encodeToString(b, Base64.DEFAULT)
                    alertEditText(imgString)
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }
    }

    private fun authUser(imagen: String,codigo:String) {
        getToken()
        getLocation()
        if (!NetworkUtils.isConnected(this@LoginActivity)) {
            Toast.makeText(this@LoginActivity, R.string.error_internet, Toast.LENGTH_LONG).show()
        } else {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val queue = Volley.newRequestQueue(this)
            val stringRequest = object : StringRequest(Method.POST, "${URL_SERVER}login", Response.Listener<String> { response ->
                try {
                    val json: JsonObject = Parser.default().parse(StringBuilder(response)) as JsonObject
                    val result = Klaxon().parseFromJsonObject<User>(json.obj("guardias")!!)
                    val supervisor = json.obj("guardias")!!.int("supervisor")

                    if(supervisor == 1){
                        Toast.makeText(this, resources.getString(R.string.error_supervisor), Toast.LENGTH_LONG).show()
                        progressView.visibility = View.GONE
                        contentView.visibility = View.VISIBLE
                    }else{

                        val horario = Klaxon().parseFromJsonObject<Horario>(json.obj("horario")!!)
                        val punto = Klaxon().parseFromJsonObject<Punto>(json.obj("horario")!!.obj("punto_horario")!!.obj("punto")!!)

                        val id_guardia = json.obj("guardias")!!.int("id_guardia").toString()
                        val token = json.obj("guardias")!!.string("api_key")
                        val codigo = json.obj("guardias")!!.string("codigo")
                        val nombres = json.obj("guardias")!!.string("nombres")
                        val imagen = json.obj("guardias")!!.string("imagen")

                        prefs.edit().putString("horario", Klaxon().toJsonString(horario)).apply()
                        prefs.edit().putString("id_turno",horario!!.id_turno.toString()).apply()
                        prefs.edit().putString("punto", Klaxon().toJsonString(punto)).apply()
                        prefs.edit().putString("id_punto", punto!!.id_punto.toString()).apply()
                        prefs.edit().putString("id_guardia", id_guardia).apply()
                        prefs.edit().putString("guardias", Klaxon().toJsonString(result)).apply()
                        prefs.edit().putString("api_key", json.obj("guardias")!!.string("api_key")).apply()
                        prefs.edit().putString("guardia$id_guardia","$token,$codigo,$nombres,$imagen").apply()

                        if(prefs.contains("IdArrays")){
                            var IdArrays = prefs.getString("IdArrays","")
                            if(IdArrays.contains(",$id_guardia") || IdArrays.contains(",$id_guardia") || IdArrays == id_guardia){
                                Log.d("IdArray","ya ingreso sesion")
                            }else
                                IdArrays += ",$id_guardia"

                            prefs.edit().putString("IdArrays",IdArrays).apply()
                        }else{
                            prefs.edit().putString("IdArrays",id_guardia).apply()
                        }

                        Log.d("IdArrays",prefs.getString("IdArrays",""))
                        Log.d("guardia$id_guardia",prefs.getString("guardia$id_guardia",""))
                        startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                        finish()
                    }
                } catch (e: JSONException) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    error.printStackTrace()
                    val errorMessage = JSONObject(String(error.networkResponse.data)).getString("message")
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getParams(): MutableMap<String, String> {

                    val parameters = HashMap<String, String>()
                    parameters["lon"] = prefs.getString("lon", "")!!
                    parameters["lat"] = prefs.getString("lat", "")!!
                    parameters["token"] = prefs.getString("token", "")!!
                    parameters["codigo"] = codigo
                    parameters["imagen"] = imagen
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    fun setGuardia(view: View){
        val id_guardia = view.getTag()
        Log.d("guardia",prefs.getString("guardia$id_guardia",""))
        val guardia = prefs.getString("guardia$id_guardia","").split(",")
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.app_name)).setMessage(R.string.dialog_profile_description)

        val view = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val categoryEditText = view.findViewById(R.id.etprofile) as EditText
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            val etprofile = categoryEditText.text
            var isValid = true
            val et = etprofile.toString()
            val an = guardia[1]
            Log.d("validad","$et = $an")
            if (etprofile.isBlank()) {
                categoryEditText.error = getString(R.string.error_fields_required)
                isValid = false
            }else if(etprofile.toString() != guardia[1]){
                categoryEditText.error = getString(R.string.error_dialog)
                isValid = false
            }
            if (isValid) {
                getPerfil(guardia[0])
            }else{
                dialog.dismiss()
                Toast.makeText(this@LoginActivity, categoryEditText.error, Toast.LENGTH_LONG).show()
            }
         }
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        builder.show()
    }

    fun getPerfil(token:String){
        if (!NetworkUtils.isConnected(this@LoginActivity)) {
            Toast.makeText(this@LoginActivity, R.string.error_internet, Toast.LENGTH_LONG).show()
        } else {
            progressView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
            val queue = Volley.newRequestQueue(this)
            val stringRequest = object : StringRequest(Method.GET, "${URL_SERVER}perfil", Response.Listener<String> { response ->
                try {
                    val json: JsonObject = Parser.default().parse(StringBuilder(response)) as JsonObject
                    val result = Klaxon().parseFromJsonObject<User>(json.obj("guardias")!!)
                    val horario = Klaxon().parseFromJsonObject<Horario>(json.obj("horario")!!)
                    val punto = Klaxon().parseFromJsonObject<Punto>(json.obj("horario")!!.obj("punto_horario")!!.obj("punto")!!)
                    val id_guardia = json.obj("guardias")!!.int("id_guardia").toString()
                    prefs.edit().putString("horario", Klaxon().toJsonString(horario)).apply()
                    prefs.edit().putString("id_turno",horario!!.id_turno.toString()).apply()
                    prefs.edit().putString("punto", Klaxon().toJsonString(punto)).apply()
                    prefs.edit().putString("id_punto", punto!!.id_punto.toString()).apply()
                    prefs.edit().putString("id_guardia", id_guardia).apply()
                    prefs.edit().putString("guardias", Klaxon().toJsonString(result)).apply()
                    prefs.edit().putString("api_key", json.obj("guardias")!!.string("api_key")).apply()
                    Log.d("/perfil",response.toString())
                    startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                    finish()
                } catch (e: JSONException) {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    progressView.visibility = View.GONE
                    contentView.visibility = View.VISIBLE
                    error.printStackTrace()
                    val errorMessage = JSONObject(String(error.networkResponse.data)).getString("message")
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = java.util.HashMap<String, String>()
                    headers.put("token", token!!)
                    return headers
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }

    val PERMISSION_ID = 42
    private fun checkPermission(vararg perm:String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(this, it)}
            ) {

            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }


}
