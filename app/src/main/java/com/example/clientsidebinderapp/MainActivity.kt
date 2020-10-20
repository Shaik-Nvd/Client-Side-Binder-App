package com.example.clientsidebinderapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ServiceDemo"
        var GET_RANDOM_NUMBER_FLAG = 0
    }

    private var randomNumberValue = 0
    private var isBound = false

    private var randomNumberRequestMessenger: Messenger? = null
    private var randomNumberReceiveMessenger: Messenger? = null

    private var serviceIntent: Intent? = null


    inner class ReceiveRandomNumberHandler : Handler() {
        override fun handleMessage(msg: Message) {
            randomNumberValue = 0
            when(msg.what) {
                GET_RANDOM_NUMBER_FLAG -> { //flag to be same as implemented in the sender
                    randomNumberValue = msg.arg1
                    numberTv.text = "Random Number: $randomNumberValue"
                }
            }
            super.handleMessage(msg)
        }
    }

    private var randomNumberServiceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            randomNumberRequestMessenger = Messenger(binder)
            randomNumberReceiveMessenger = Messenger(ReceiveRandomNumberHandler())
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            randomNumberRequestMessenger = null
            randomNumberReceiveMessenger = null
            isBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceIntent = Intent()
        serviceIntent!!.component = ComponentName("com.example.servicesideapp","com.example.servicesideapp.MyService" )
        serviceIntent!!.setPackage(packageName)

        bindServiceBtn.setOnClickListener {
            bindToRemoteService()
        }

        unbindServiceBtn.setOnClickListener {
            unbindToRemoteService()
        }

        randomNumberBtn.setOnClickListener {
            fetchRandomNumber()
        }
    }

    private fun bindToRemoteService() {
        bindService(serviceIntent, randomNumberServiceConnection!!, BIND_AUTO_CREATE)
        Toast.makeText(this,"Service Bound", Toast.LENGTH_SHORT).show()
    }

    private fun unbindToRemoteService() {
        unbindService(randomNumberServiceConnection!!)
        isBound = false
        Toast.makeText(this,"Service Unbounded", Toast.LENGTH_SHORT).show()
    }


    private fun fetchRandomNumber() {
        if(isBound) {
            val requestMessage = Message.obtain(null, GET_RANDOM_NUMBER_FLAG)
            requestMessage.replyTo = randomNumberReceiveMessenger
            try {
                randomNumberReceiveMessenger?.send(requestMessage)
            } catch (e: RemoteException) {
                Log.i(TAG, ""+e.message)
            }
        }  else {
            Toast.makeText(this," Service Unbounded, can't get random number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        randomNumberServiceConnection = null
    }
}