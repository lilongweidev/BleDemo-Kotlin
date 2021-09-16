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
        const val SERVICE_UUID = "5833ff01-9b8b-5191-6142-22a4536ef123"
        /**
         * 描述 UUID
         */
        const val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

        /**
         * 特征（特性）写入 UUID
         */
        const val CHARACTERISTIC_WRITE_UUID = "5833ff02-9b8b-5191-6142-22a4536ef123"

        /**
         * 特征（特性）表示 UUID
         */
        const val CHARACTERISTIC_INDICATE_UUID = "5833ff03-9b8b-5191-6142-22a4536ef123"
    }
}