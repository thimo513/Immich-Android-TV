package nl.giejay.android.tv.immich.plus.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast
import androidx.core.content.IntentCompat
import nl.giejay.android.tv.immich.R
import timber.log.Timber

/** Receives the result of the install session started by [UpdateChecker]. */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // the system confirmation dialog ("Install this update?")
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java) ?: return
                context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            PackageInstaller.STATUS_SUCCESS -> Timber.i("Update installed")

            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: status.toString()
                Timber.e("Update failed: $message")
                Toast.makeText(context, context.getString(R.string.plus_update_failed, message), Toast.LENGTH_LONG).show()
            }
        }
    }
}
