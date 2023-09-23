package com.example.printhelloworld

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.printhelloworld.databinding.ActivityMainBinding
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binder: ActivityMainBinding
    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        binder.printButton.setOnClickListener { printHelloWorld() }

        // Request Bluetooth permissions at runtime if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, you can now proceed to connect to the printer
                connectToBluetoothPrinter()
            } else {
                // Permissions not granted, handle it accordingly (e.g., show a message)
            }
        }
    }

    private fun connectToBluetoothPrinter() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        pairedDevices?.forEach { device ->
        //    if (device.name == "Your_Printer_Name") { // Replace with your printer's name
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    )
                    bluetoothSocket?.connect()
                    outputStream = bluetoothSocket?.outputStream
                } catch (e: IOException) {
                    e.printStackTrace()
                }

        }
    }

    private fun printText(text: String) {
        try {
            outputStream?.write(text.toByteArray())
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun printHelloWorld() {
        connectToBluetoothPrinter()

        try {
            // Check if the Bluetooth socket and output stream are not null
            if (bluetoothSocket != null && outputStream != null) {
                val message = "Hello, World!\n"
                val textSizes = arrayOf(10, 20, 30, 40, 50, 60) // Different text sizes (1 to 6)

                for (size in textSizes) {
                    // Create an ESC/POS command to set the text size
                    val textSizeCommand = byteArrayOf(0x1B, 0x21, size.toByte())

                    // Create an ESC/POS command to enable italic style
                    val italicStyleCommand = byteArrayOf(0x1B, 0x34, 0x01)

                    // Send the text size and italic style commands to the printer
                    outputStream?.write(textSizeCommand)
                    outputStream?.write(italicStyleCommand)

                    // Print "Hello, World!" with the current text size and italic style
                    printText(message)

                    // Reset text size and disable italic style
                    val resetTextSizeAndStyleCommand = byteArrayOf(0x1B, 0x21, 0x00)
                    outputStream?.write(resetTextSizeAndStyleCommand)
                }

                // Close the output stream and socket
                outputStream?.close()
                bluetoothSocket?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



}
