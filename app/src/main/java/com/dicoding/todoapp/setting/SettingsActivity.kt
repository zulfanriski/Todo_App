package com.dicoding.todoapp.setting

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dicoding.todoapp.R
import com.dicoding.todoapp.notification.NotificationWorker
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Notifications permission granted")
            } else {
                showToast("Notifications will not show without permission")
            }
        }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefNotification = findPreference<SwitchPreference>(getString(R.string.pref_key_notify))
            prefNotification?.setOnPreferenceChangeListener { preference, newValue ->
                val channelName = getString(R.string.notify_channel_name)
                //TODO 13 : Schedule and cancel daily reminder using WorkManager with data channelName
                if (newValue == true){
                    tambahPengingat(channelName, requireContext())
                }else{
                    batalPengingat(requireContext())
                }


                true
            }

        }
        private fun batalPengingat(context: Context){
            val wM = WorkManager.getInstance(context)
            wM.cancelUniqueWork(NAME)
        }

        private fun tambahPengingat(channelName: String, context: Context) {
            val workManager = WorkManager.getInstance(context)


            val repeatInterval = 24L * 60L * 60L
            val flexInterval = 24L * 60L * 60L

            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                NotificationWorker::class.java,
                repeatInterval, TimeUnit.SECONDS
            )
                .setInitialDelay(flexInterval, TimeUnit.SECONDS) 
                .addTag(TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                channelName,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
        }



        companion object{
            const val TAG = "w1"
            const val NAME = "Notifikasi_Harian"
        }
    }

}