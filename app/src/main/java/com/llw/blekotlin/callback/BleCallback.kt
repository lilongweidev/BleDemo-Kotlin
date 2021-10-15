package com.llw.blekotlin.callback

import android.bluetooth.*
import android.os.Build
import android.util.Log
import com.llw.blekotlin.utils.BleConstant
import com.llw.blekotlin.utils.BleHelper
import com.llw.blekotlin.utils.ByteUtils
import java.util.*

/**
 * Ble回调
 * @description BleCallback
 * @author llw
 * @date 2021/9/13 23:32
 */
class BleCallback : BluetoothGattCallback() {
    private val TAG = BleCallback::class.java.simpleName
    private lateinit var uiCallback: UiCallback

    fun setUiCallback(uiCallback: UiCallback) {
        this.uiCallback = uiCallback
    }

    /**
     * 连接状态回调
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "onConnectionStateChange: $status")
            return
        }
        uiCallback.state(
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    //获取MtuSize
                    gatt.requestMtu(512)
                    "连接成功"
                }
                BluetoothProfile.STATE_DISCONNECTED -> "断开连接"
                else -> "onConnectionStateChange: $status"
            }
        )
    }

    /**
     * 获取MtuSize回调
     */
    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        uiCallback.state("获取到MtuSize：$mtu")
        //发现服务
        gatt.discoverServices()
    }

    /**
     * 发现服务回调
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        uiCallback.state(if (!BleHelper.enableIndicateNotification(gatt)) { gatt.disconnect()
            "开启通知属性异常"
        } else "发现了服务 code: $status")
    }

    /**
     * 特性改变回调
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val content = ByteUtils.bytesToHexString(characteristic.value)
        uiCallback.state("收到：$content")
    }

    /**
     * 特性写入回调
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        val command = ByteUtils.bytesToHexString(characteristic.value)
        uiCallback.state("发出: ${if (status == BluetoothGatt.GATT_SUCCESS) "成功：" else "失败："}$command code: $status")
    }

    /**
     * 描述符写入回调
     */
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (BleConstant.DESCRIPTOR_UUID == descriptor.uuid.toString().lowercase(Locale.getDefault())) {
            uiCallback.state(if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) readPhy()
                    readDescriptor(descriptor)
                    readRemoteRssi() }
                "通知开启成功"
            } else "通知开启失败")
        }
    }

    /**
     * 读取远程设备的信号强度回调
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) = uiCallback.state("onReadRemoteRssi: rssi: $rssi")

    /**
     * UI回调
     */
    interface UiCallback {
        /**
         * 当前Ble状态信息
         */
        fun state(state: String)
    }
}