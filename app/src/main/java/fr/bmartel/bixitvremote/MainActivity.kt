/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2017-2018 Bertrand Martel
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.bixitvremote

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log

/**
 * Main activity.
 *
 * @author Bertrand Martel
 */
class MainActivity : Activity(), ServiceConnection {

    lateinit var remoteTvService: RemoteTvService

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val SERVICE_NAME = "fr.bmartel.bixitvremote.RemoteTvService"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        remoteTvService.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        remoteTvService = (service as RemoteTvService.LocalBinder).service
        remoteTvService.init(this)
    }

    override fun onServiceDisconnected(name: ComponentName) {}

    /**
     * bind to Bixi service.
     */
    private fun bindService() {
        val bixiServiceIntent = Intent()
        bixiServiceIntent?.setClassName(this, SERVICE_NAME)

        startService(bixiServiceIntent)

        val bound = bindService(bixiServiceIntent, this,
                Context.BIND_AUTO_CREATE)

        if (bound) {
            Log.v(TAG, "service started")
        } else {
            Log.e(TAG, "service not started")
        }
    }
}
