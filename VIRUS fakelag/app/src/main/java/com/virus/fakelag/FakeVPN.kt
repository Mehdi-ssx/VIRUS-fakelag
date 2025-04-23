package com.virus.fakelag

import android.content.*
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.IOException

object FakeVPN {
    private var vpnInterface: ParcelFileDescriptor? = null

    fun start(context: Context) {
        val builder = VpnService.Builder()
        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.setSession("FakeVPN")
        vpnInterface = builder.establish()
    }

    fun stop(context: Context) {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}