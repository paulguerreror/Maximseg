package com.neobit.maximseg.Utils

import com.google.android.gms.location.LocationResult
import android.content.Intent
import android.app.IntentService
import android.util.Log

class LocationUpdatesIntentService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations
                    val locationResultHelper = LocationResultHelper(
                        this,
                        locations
                    )
                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults()
                    // Show notification with the location data.
                    locationResultHelper.showNotification()
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(this))
                }
            }
        }
    }

    companion object {

        internal val ACTION_PROCESS_UPDATES =
            "com.neobit.maximseg.Utils.action" + ".PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }
}// Name the worker thread.