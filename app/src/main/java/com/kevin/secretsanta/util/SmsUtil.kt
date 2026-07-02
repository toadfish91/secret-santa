package com.kevin.secretsanta.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat

object SmsUtil {
    fun hasSmsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        if (!hasSmsPermission(context)) return false
        return try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send to $phoneNumber: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
