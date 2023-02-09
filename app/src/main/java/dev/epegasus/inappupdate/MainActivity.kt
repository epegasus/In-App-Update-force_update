package dev.epegasus.inappupdate

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.install.model.AppUpdateType
import dev.epegasus.inappupdate.databinding.ActivityMainBinding
import dev.epegasus.inappupdate.manager.PegasusAppUpdateManager

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val pegasusAppUpdateManager = PegasusAppUpdateManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkForUpdate()
    }

    /**
     *    Types of AppUpdate:
     *     -> AppUpdateType.IMMEDIATE
     *     -> AppUpdateType.FLEXIBLE
     */

    private fun checkForUpdate() {
        pegasusAppUpdateManager.setUpdateType(AppUpdateType.IMMEDIATE)
        pegasusAppUpdateManager.checkForUpdate { isAvailable, message ->
            binding.tvText.text = message
            if (isAvailable) {
                requestForUpdate()
            }
        }
    }

    private fun requestForUpdate() {
        pegasusAppUpdateManager.requestForUpdate { isUpdated, message ->
            binding.tvText.text = message
            if (isUpdated) {
                // Proceed with Code...
                Log.d("TAG", "requestForUpdate: Running App...")
            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'
    override fun onResume() {
        super.onResume()
        pegasusAppUpdateManager.checkIfUpdateInstalled()
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}