package com.neobit.maximseg

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Build
import androidx.appcompat.app.AlertDialog

import android.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.content.SharedPreferences
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import com.neobit.maximseg.Utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.neobit.maximseg.Utils.NetworkUtils
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.Manifest
import android.graphics.Bitmap
import android.location.LocationManager
import android.provider.MediaStore
import android.util.Base64
import  com.neobit.maximseg.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.neobit.maximseg.R.anim.enter_from_left
import org.json.JSONObject
import java.io.ByteArrayOutputStream

import java.util.HashMap
private var locationManager : LocationManager? = null



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var prefs: SharedPreferences

    var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocationRequest: LocationRequest
    private val UPDATE_INTERVAL = (10).toLong()
    private val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 3
    private val REQUEST_IMAGE_CAPTURE = 1356

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext!!)
        getLocationUpdates()
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        getToken()
        onNavigationItemSelected(navView.menu.getItem(0))
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            /*
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
*/

            val fragment = NovedadCrearFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_left).replace(R.id.frame_container, fragment).commit()
        }
    }

    fun getToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("tk", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                // Log and toast
                Log.d("firebase", token)
            })
    }

    fun hidefab(hide: Boolean){
        /*
        val fab: FloatingActionButton = findViewById(R.id.fab)
        if(hide){
            fab.hide()
        }else{
            fab.show()
        }
         */
    }

    fun markTarea(view: View){
        Log.d("tarea",view.getTag().toString())
        if(view.getTag().toString().contains("i")){
            val id = view.getTag().toString().replace("i","")
            prefs.edit().putString("id_tarea",id).apply()
            startActivity(Intent(this, TareasActivity::class.java))
        }else{
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_end_tarea)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    dialog.cancel()
                    endTarea(view.getTag().toString())
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            true
        }
    }

    fun endTarea(id: String){
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else{
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}tareas/$id"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {

                        var strResp = response.toString()
                        val jsonObj: JSONObject = JSONObject(strResp)
                        Log.d(URL,strResp)
                        val message = jsonObj.get("message").toString()
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        displayView(1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
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

    fun getParadas(view:View){
       prefs.edit().putString("id_ronda",view.getTag().toString()).apply()
        Log.d("id_ronda",prefs.getString("id_ronda",""))
        //startActivity(Intent(this, RondaActivity::class.java))
        val fragment = ParadasFragment()
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left).replace(R.id.frame_container, fragment).commit()
    }

    fun getNovedadDetalles(view: View){
        prefs.edit().putString("id_novedad",view.getTag().toString()).apply()
        startActivity(Intent(this, NovedadDetallesActivity::class.java))
    }

    fun setBar(title : String,back: Boolean){
        val actionbar = supportActionBar
        actionbar!!.title= title
        if(back){
            actionbar!!.setDisplayHomeAsUpEnabled(true)
        }
    }
/*
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }*/

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val anonymousFragment = supportFragmentManager.findFragmentById(R.id.frame_container)
            if (anonymousFragment is NovedadCrearFragment) {
                val fragmentManager = supportFragmentManager
                if(prefs.getString("cliente","") == "1"){
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_container, NovedadesFragment()).commit()
                }else{
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_container, NovedadesPrivadaFragment()).commit()
                }
            } else if (anonymousFragment is ParadasFragment) {
                val fragment = RondasFragment()
                val fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).replace(R.id.frame_container, fragment).commit()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle(getString(R.string.app_name)).setMessage(R.string.alert_end_turno)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                            dialog.cancel()
                            endTurno()
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
                    val alert = builder.create()
                    alert.show()
                    true
                }
            else -> super.onOptionsItemSelected(item)
        }
    }

     fun logOut(view: View) {
        clearInfo()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun endTurno() {
        if(prefs.contains("ronda_historial")){
            Toast.makeText(applicationContext, R.string.error_round,Toast.LENGTH_LONG).show()
        }else{
        if (!NetworkUtils.isConnected(applicationContext)) {
            Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
        } else {
            val queue = Volley.newRequestQueue(applicationContext)
            val URL = "${Utils.URL_SERVER}logout"
            val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                try {
                    val json = JSONObject(response.replace("ï»¿", ""))
                    val id_guardia = prefs.getString("id_guardia","")
                    prefs.edit().remove("guardia$id_guardia").commit()
                    var IdArrays = prefs.getString("IdArrays","")
                    if(IdArrays.contains(",$id_guardia")){
                        IdArrays = IdArrays.replace(",$id_guardia","")
                        prefs.edit().putString("IdArrays",IdArrays).apply()
                    }else if(IdArrays.contains("$id_guardia,")){
                        IdArrays = IdArrays.replace("$id_guardia,","")
                        prefs.edit().putString("IdArrays",IdArrays).apply()
                    }else if(IdArrays == id_guardia){
                        prefs.edit().remove("IdArrays").commit()
                    }else{
                        Log.d("error","error al terminar turno")
                    }
                    clearInfo()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    Toast.makeText(applicationContext, json.getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener { error ->
                try {
                    error.printStackTrace()
                    Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
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
                    parameters["lat"] = prefs.getString("lat", "")!!
                    parameters["lon"] = prefs.getString("lon", "")!!
                    return parameters
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(stringRequest)
        }
    }
    }

    fun clearInfo(){
        prefs.edit().remove("horario").commit()
        prefs.edit().remove("id_turno").commit()
        prefs.edit().remove("punto").commit()
        prefs.edit().remove("id_punto").commit()
        prefs.edit().remove("id_guardia").commit()
        prefs.edit().remove("guardias").commit()
        prefs.edit().remove("ronda").commit()
        prefs.edit().remove("ronda_historial").commit()
    }

    fun checkAviso(view: View){
        val id= view.getTag(R.id.id).toString()
        Log.d("aviso",id)
        prefs.edit().putString("id_aviso",id).apply()
        dispatchTakePictureIntent()
    }

    fun dispatchTakePictureIntent() {
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
                    Toast.makeText(applicationContext, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).
                withErrorListener{ Toast.makeText(applicationContext, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
            .onSameThread()
            .check()
    }

    fun saveAviso(imagen: String) {
            if (!NetworkUtils.isConnected(applicationContext)) {
                Toast.makeText(applicationContext, R.string.error_internet2, Toast.LENGTH_LONG).show()
            } else {
                val queue = Volley.newRequestQueue(applicationContext)
                val URL = "${Utils.URL_SERVER}avisos/${prefs.getString("id_aviso","")}"
                val stringRequest = object : StringRequest(Method.POST, URL, Response.Listener<String> { response ->
                    try {
                        val json = JSONObject(response)
                        Log.d(URL,json.toString())
                        Toast.makeText(applicationContext, json.getString("mensaje"), Toast.LENGTH_LONG).show()
                        displayView(6)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, resources.getString(R.string.error_general), Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener { error ->
                    try {
                        error.printStackTrace()
                        Toast.makeText(applicationContext, JSONObject(String(error.networkResponse.data)).getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
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
                        parameters["imagen"] = imagen
                        return parameters
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(180000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(stringRequest)
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> displayView(0)
            R.id.nav_tasks-> displayView(1)
            R.id.nav_rounds-> displayView(2)
            R.id.nav_obligations-> displayView(3)
            R.id.nav_reports-> displayView(4)
            R.id.nav_private_reports-> displayView(5)
            R.id.nav_avisos-> displayView(6)

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun displayView(position: Int) {
        var fragment: Fragment? = null
        val bundl = Bundle()
        when (position) {
            0 -> {
                fragment = ProfileFragment()
            }
            1 -> {
                fragment = TareasFragment()
            }
            2 -> {
                fragment = RondasFragment()
            }
            3 -> {
                fragment = ObligacionesFragment()
            }
            4 -> {
                fragment = NovedadesFragment()
            }
            5 -> {
                fragment = NovedadesPrivadaFragment()
            }
            6 -> {
                fragment = AvisosFragment()
            }
        }
        if (fragment != null) {
            fragment!!.arguments = bundl
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
        } else Log.e("MainActivity", "Error in creating fragment")
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
                /*
                Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object: PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                        }
                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Toast.makeText(activity, R.string.error_permissions, Toast.LENGTH_SHORT).show()
                        }
                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                            token!!.continuePermissionRequest()
                        }
                    }).
                        withErrorListener{ Toast.makeText(activity, R.string.error_permissions, Toast.LENGTH_SHORT).show()}
                    .onSameThread()
                    .check()

                 */
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_ID -> {
                Log.d("permission","request")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(supportFragmentManager?.fragments!=null && supportFragmentManager?.fragments!!.size>0)
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null) {
                    val imageBitmap = data.extras.get("data") as Bitmap
                    val baos = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    val imgString = Base64.encodeToString(b, Base64.DEFAULT)
                    saveAviso(imgString)
                }else{
                    for (i in 0..supportFragmentManager?.fragments!!.size-1) {
                        val fragment= supportFragmentManager?.fragments!!.get(i)
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }

        super.onActivityResult(requestCode, resultCode, data)
    }
    }


     fun getLocationUpdates()
    {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext!!)
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                    prefs.edit().putString("lat", location.latitude.toString()).apply()
                    prefs.edit().putString("lon", location.longitude.toString()).apply()
                    Log.d("Coordenadas:", "${prefs.getString("lat","")}, ${prefs.getString("lon","")}")
                    //Toast.makeText(applicationContext,"Nuevas Coordenadas: ${prefs.getString("lat","")}, ${prefs.getString("lon","")}" , Toast.LENGTH_LONG).show()
                }
            }
        }
        }
    }

    //start location updates
    private fun startLocationUpdates() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

}
