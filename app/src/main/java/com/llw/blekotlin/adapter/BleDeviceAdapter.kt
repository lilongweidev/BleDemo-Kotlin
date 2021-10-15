package com.llw.blekotlin.adapter

import android.annotation.SuppressLint
import com.llw.blekotlin.bean.BleDevice
import com.llw.blekotlin.databinding.ItemBluetoothBinding

/**
 * Ble设备适配器 传入ViewBinding
 * @description BleDeviceAdapter
 * @author llw
 * @date 2021/9/10 12:28
 */
class BleDeviceAdapter(data: MutableList<BleDevice>? = null) :
    ViewBindingAdapter<ItemBluetoothBinding, BleDevice>(data) {
    @SuppressLint("SetTextI18n")
    override fun convert(holder: ViewBindingHolder<ItemBluetoothBinding>, item: BleDevice) {
        val binding = holder.vb
        binding.tvDeviceName.text = item.name
        binding.tvMacAddress.text = item.device.address
        binding.tvRssi.text = "${item.rssi} dBm"
    }
}