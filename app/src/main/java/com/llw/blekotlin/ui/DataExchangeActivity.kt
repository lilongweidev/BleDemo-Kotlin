package com.llw.blekotlin.ui

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.view.MenuItem
import android.view.View.FOCUS_DOWN
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.llw.blekotlin.callback.BleCallback
import com.llw.blekotlin.databinding.ActivityDataExchangeBinding
import com.llw.blekotlin.utils.BleHelper
import com.llw.blekotlin.utils.ByteUtils.getBCCResult

class DataExchangeActivity : AppCompatActivity(), BleCallback.UiCallback {

    //视图绑定
    private lateinit var binding: ActivityDataExchangeBinding
    //Gatt
    private lateinit var gatt: BluetoothGatt
    //Ble回调
    private val bleCallback = BleCallback()
    //状态缓存
    private var stringBuffer = StringBuffer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataExchangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //初始化
        initView()
    }

    /**
     * 初始化
     */
    private fun initView() {
        supportActionBar?.apply {
            title = "Data Exchange"
            setDisplayHomeAsUpEnabled(true)
        }
        val device = intent.getParcelableExtra<BluetoothDevice>("device")
        //gatt连接
        gatt = device!!.connectGatt(this, false, bleCallback)
        //发送指令
        binding.btnSendCommand.setOnClickListener {
            var command = binding.etCommand.text.toString().trim()
            if (command.isEmpty()) {
                showMsg("请输入指令")
                return@setOnClickListener
            }
            command += getBCCResult(command)
            //发送指令
            BleHelper.sendCommand(gatt, command)
        }
        //Ble状态页面UI回调
        bleCallback.setUiCallback(this)
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }


    //页面返回
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home)  { onBackPressed();true } else false

    override fun state(state: String) = runOnUiThread {
        stringBuffer.append(state).append("\n")
        binding.tvState.text = stringBuffer.toString()
        binding.scroll.apply { viewTreeObserver.addOnGlobalLayoutListener { post { fullScroll(FOCUS_DOWN) } } }
    }


}