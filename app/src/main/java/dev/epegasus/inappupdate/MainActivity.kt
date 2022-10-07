package dev.epegasus.inappupdate

import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dev.epegasus.inappupdate.databinding.ActivityMainBinding


private const val MY_REQUEST_CODE = 1000
private const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkForUpdate()
    }

    private fun checkForUpdate() {

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        // This example applies an immediate update. To apply a flexible update instead, pass in AppUpdateType.FLEXIBLE
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UNKNOWN -> showToast("Unknown Response")
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> showToast("No Update Available")
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> showToast("Update is in Progress. Please Resume")
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    showToast("Update Available")
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) requestForUpdate(appUpdateManager, appUpdateInfo)
                }
            }
        }
    }

    /**
     * @see : MY_REQUEST_CODE -> Ignore this Constant
     */

    private fun requestForUpdate(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        // Pass the intent that is returned by 'getAppUpdateInfo()'.
        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
        // The current activity making the update request.
        // Include a request code to later monitor this update request.
        val starter = IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent).setFillInIntent(fillInIntent).setFlags(flagsValues, flagsMask).build()
            updateFlowResultLauncher.launch(request)
        }
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, starter, MY_REQUEST_CODE)
        } catch (ex: SendIntentException) {
            showToast("Exception Found")
            Log.d(TAG, "requestForUpdate: $ex")
        }
    }

    private val updateFlowResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            showToast("Updated Successfully")
        } else {
            showToast("Update Provoked")
        }
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all app entry points.
    override fun onResume() {
        super.onResume()
        checkIfUpdateInstalled()
    }

    private fun checkIfUpdateInstalled() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }
    }

    // Displays the snackbar notification and call to action.
    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(binding.root, "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            show()
        }
    }
}