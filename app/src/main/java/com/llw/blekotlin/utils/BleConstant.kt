package com.llw.blekotlin.utils

/**
 *
 * @description BleConstant
 * @author llw
 * @date 2021/9/13 14:12
 */
class BleConstant {

    companion object BleConstant {
        /**
         * 服务 UUID
         */
        const val SERVICE_UUID = "service_uuid"
        /**
         * 描述 UUID
         */
        const val DESCRIPTOR_UUID = "descriptor_uuid"

        /**
         * 特征（特性）写入 UUID
         */
        const val CHARACTERISTIC_WRITE_UUID = "characteristic_write_uuid"

        /**
         * 特征（特性）表示 UUID
         */
        const val CHARACTERISTIC_INDICATE_UUID = "characteristic_indicate_uuid"

        /**
         * 是否过滤设备名称为Null的设备
         */
        const val NULL_NAME = "nullName"

        /**
         * 过滤信号强度值
         */
        const val RSSI = "rssi"
    }

}