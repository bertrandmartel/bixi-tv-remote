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
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import fr.bmartel.android.bixi.client.BixiClient
import fr.bmartel.android.bixi.inter.IBixiListener
import fr.bmartel.android.bixi.inter.IGestureListener
import fr.bmartel.android.bixi.model.BixiEvent
import fr.bmartel.android.bixi.model.BixiGesture
import fr.bmartel.android.bixi.model.BtDevice
import java.io.DataOutputStream
import android.media.AudioManager
import android.os.Build


/**
 * Service connecting to Bixi device.
 *
 * @author Bertrand Martel
 */
class RemoteTvService : Service() {

    /**
     * Service binder
     */
    private val mBinder = LocalBinder()

    private lateinit var bixiClient: BixiClient

    private lateinit var inputManager: InputManager

    /*
     * LocalBinder that render public getService() for public access
     */
    inner class LocalBinder : Binder() {
        val service: RemoteTvService
            get() = this@RemoteTvService
    }

    override fun onCreate() {
        bixiClient = BixiClient(this, object : IBixiListener {
            override fun onServiceConnected() {
                Log.v("bixi-lib", "service is connected - ready to receive events")

                //start scanning
                bixiClient.startScan()
            }

            override fun onStartScan() {
                Log.v("bixi-lib", "scan started")
            }

            override fun onEndScan() {
                Log.v("bixi-lib", "scan stopped")
            }

            override fun onDeviceDiscovered(device: BtDevice) {
                Log.v("bixi-lib", "new Bixi device discovered : " + device.deviceName)

                //stop scanning
                bixiClient.stopScan()

                //connect to this device
                bixiClient.connectDevice(device.deviceAddress)
            }

            override fun onDeviceDisconnected(device: BtDevice) {
                Log.v("bixi-lib", "device disconnected : " + device.deviceName)
            }

            override fun onDeviceConnected(device: BtDevice) {
                Log.v("bixi-lib", "device connected : " + device.deviceName)

                //set a listener to be notified when a gesture event occur
                bixiClient.getDevice(device)?.setBixiGestureListener(object : IGestureListener {
                    override fun onGestureChange(event: BixiEvent) {
                        Log.v("bixi-lib", "received gesture event : " + event.gesture)
                        when (event.gesture) {
                            BixiGesture.CENTER_TO_TOP -> sendKeyCode(keyCode = KeyEvent.KEYCODE_DPAD_UP)
                            BixiGesture.CENTER_TO_BOTTOM -> sendKeyCode(keyCode = KeyEvent.KEYCODE_DPAD_DOWN)
                            BixiGesture.CENTER_TO_LEFT -> sendKeyCode(keyCode = KeyEvent.KEYCODE_DPAD_LEFT)
                            BixiGesture.CENTER_TO_RIGHT -> sendKeyCode(keyCode = KeyEvent.KEYCODE_DPAD_RIGHT)
                            BixiGesture.DOUBLE_TAP -> sendKeyCode(keyCode = KeyEvent.KEYCODE_ENTER)
                            BixiGesture.LINEAR_CHANGE -> setVolume(value = event.linearValue)
                        }
                    }
                })
            }

            override fun onBluetoothOff() {
                Log.v("bixi-lib", "bluetooth hasn't been activated (user refused the popup)")
            }

            override fun onPermissionDenied() {
                Log.v("bixi-lib", "location permission was denied (necessary to scan)")
            }
        })
        inputManager = getSystemService(Context.INPUT_SERVICE) as InputManager
    }

    fun init(activity: Activity) {
        bixiClient.init(activity)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        bixiClient.onRequestPermissionsResult(requestCode, grantResults)
    }

    fun onActivityResult(requestCode: Int) {
        bixiClient.onActivityResult(requestCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        bixiClient.disconnectAll()
    }

    fun sendKeyCode(keyCode: Int) {
        sendCommand(command = "input keyevent " + keyCode)
    }

    fun setVolume(value: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volumeVal: Int = (value * audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)) / 19

        if (volumeVal > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeVal, AudioManager.FLAG_SHOW_UI)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
            }
        }
    }

    private fun sendCommand(command: String): Boolean {
        val shell = Runtime.getRuntime().exec("su")
        val commandLine = DataOutputStream(shell.outputStream)
        commandLine.writeBytes(command + '\n')
        commandLine.flush()
        commandLine.writeBytes("exit\n")
        commandLine.flush()
        return shell.waitFor() == 0
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
}