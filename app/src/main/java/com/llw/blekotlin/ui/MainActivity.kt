package com.llw.blekotlin.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter.AnimationType
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.llw.blekotlin.R
import com.llw.blekotlin.adapter.BleDeviceAdapter
import com.llw.blekotlin.bean.BleDevice
import com.llw.blekotlin.databinding.ActivityMainBinding
import com.llw.blekotlin.databinding.DialogScanFilterBinding
import com.llw.blekotlin.databinding.DialogUuidEditBinding
import com.llw.blekotlin.utils.*
import com.llw.blekotlin.utils.BleConstant.BleConstant.NULL_NAME
import com.llw.blekotlin.utils.BleConstant.BleConstant.RSSI
import com.permissionx.guolindev.PermissionX
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.*

/**
 * 扫描
 */
class MainActivity : AppCompatActivity() {

    //视图绑定
    private lateinit var binding: ActivityMainBinding

    //默认蓝牙适配器
    private var defaultAdapter = BluetoothAdapter.getDefaultAdapter()

    //低功耗蓝牙适配器
    private lateinit var bleAdapter: BleDeviceAdapter

    //蓝牙列表
    private var mList: MutableList<BleDevice> = ArrayList()

    //地址列表
    private var addressList: MutableList<String> = ArrayList()

    //当前是否扫描
    private var isScanning = false

    //当前扫描设备是否过滤设备名称为Null的设备
    private var isScanNullNameDevice = false

    //当前扫描设备是否过滤设备信号值强度低于目标值的设备
    private var rssi = -100

    //注册开启蓝牙  注意在onCreate之前注册
    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) showMsg(if (defaultAdapter.isEnabled) "蓝牙已打开" else "蓝牙未打开")
        }

    //扫描结果回调
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.device.name ?: "Unknown"
            addDeviceList(BleDevice(result.device, result.rssi, name))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //检查版本
        checkAndroidVersion()
        //初始化页面
        initView()
//        //BCC校验
//        val a = "FCFD0401010000"
//        val b = "FCFD0703210921125122"
//        val xor = getBCCResult(a)
//        val xor1 = getBCCResult(b)
//
//        Log.d("MainActivity", "onCreate: $xor,$xor1")
    }

    /**
     * 初始化
     */
    private fun initView() {
        bleAdapter = BleDeviceAdapter(mList).apply {
            setOnItemClickListener { _, _, position ->
                if (checkUuid()) {
                    stopScan()
                    val device = mList[position].device
                    //传递数据
                    startActivity(Intent(this@MainActivity,
                        DataExchangeActivity::class.java).putExtra("device", device))
                }
            }
            animationEnable = true
            setAnimationWithDefault(AnimationType.SlideInRight)
        }
        binding.rvDevice.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bleAdapter
        }
        //扫描蓝牙
        binding.fabAdd.setOnClickListener { if (isScanning) stopScan() else scan() }
    }

    /**
     * 检查UUID
     */
    private fun checkUuid(): Boolean {
        val serviceUuid = getString(BleConstant.SERVICE_UUID)
        val descriptorUuid = getString(BleConstant.DESCRIPTOR_UUID)
        val writeUuid = getString(BleConstant.CHARACTERISTIC_WRITE_UUID)
        val indicateUuid = getString(BleConstant.CHARACTERISTIC_INDICATE_UUID)
        if (serviceUuid.isNullOrEmpty()) {
            showMsg("请输入Service UUID")
            return false
        }
        if (serviceUuid.length < 32) {
            showMsg("请输入正确的Service UUID")
            return false
        }
        if (descriptorUuid.isNullOrEmpty()) {
            showMsg("请输入Descriptor UUID")
            return false
        }
        if (descriptorUuid.length < 32) {
            showMsg("请输入正确的Descriptor UUID")
            return false
        }
        if (writeUuid.isNullOrEmpty()) {
            showMsg("请输入Characteristic Write UUID")
            return false
        }
        if (writeUuid.length < 32) {
            showMsg("请输入正确的Characteristic Write UUID")
            return false
        }
        if (indicateUuid.isNullOrEmpty()) {
            showMsg("请输入Characteristic Indicate UUID")
            return false
        }
        if (indicateUuid.length < 32) {
            showMsg("请输入正确的Characteristic Indicate UUID")
            return false
        }
        return true
    }

    /**
     * 创建菜单
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * 菜单点击
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_scan_filter -> showScanFilterDialog()
            R.id.item_service_characteristics -> showUuidEditDialog()
            else -> showMsg("Do nothing...")
        }
        return true
    }

    /**
     * 显示扫描过滤弹窗
     */
    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showScanFilterDialog() =
        BottomSheetDialog(this, R.style.BottomSheetDialogStyle).apply {
            setContentView(DialogScanFilterBinding.bind(
                View.inflate(context, R.layout.dialog_scan_filter, null)).apply {
                switchDeviceName.setOnCheckedChangeListener { _, isChecked -> isChecked.putBoolean(NULL_NAME) }
                sbRssi.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        tvRssi.text = "-$progress dBm"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        seekBar.progress.putInt(RSSI)
                    }
                })
                tvClose.setOnClickListener { dismiss() }
                //显示效果
                switchDeviceName.isChecked = getBoolean(NULL_NAME)
                //对同一个值进行配置，显示在Seekbar和TextView上
                getInt(RSSI, 100).apply { sbRssi.progress = this; tvRssi.text = "-$this dBm" }
            }.root)
            window?.findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
        }.show()

    /**
     * uuid编辑弹窗
     */
    private fun showUuidEditDialog() =
        BottomSheetDialog(this, R.style.BottomSheetDialogStyle).apply {
            setContentView(DialogUuidEditBinding.bind(View.inflate(context, R.layout.dialog_uuid_edit, null)).apply {
                    tvSave.setOnClickListener {
                        etServiceUuid.text.toString().apply { if (isNotEmpty()) putString(BleConstant.SERVICE_UUID) }
                        etDescriptorUuid.text.toString().apply { if (isNotEmpty()) putString(BleConstant.DESCRIPTOR_UUID) }
                        etCharacteristicWriteUuid.text.toString().apply { if (isNotEmpty()) putString(BleConstant.CHARACTERISTIC_WRITE_UUID) }
                        etCharacteristicIndicateUuid.text.toString().apply { if (isNotEmpty()) putString(BleConstant.CHARACTERISTIC_INDICATE_UUID) }
                        dismiss()
                    }
                    tvClose.setOnClickListener { dismiss() }
                    //显示之前设置过的uuid
                    etServiceUuid.setText(getString(BleConstant.SERVICE_UUID))
                    etDescriptorUuid.setText(getString(BleConstant.DESCRIPTOR_UUID))
                    etCharacteristicWriteUuid.setText(getString(BleConstant.CHARACTERISTIC_WRITE_UUID))
                    etCharacteristicIndicateUuid.setText(getString(BleConstant.CHARACTERISTIC_INDICATE_UUID))
                }.root)
            window?.findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
        }.show()


    /**
     * 添加到设备列表
     */
    private fun addDeviceList(bleDevice: BleDevice) {
        //过滤设备名为null的设备
        if (getBoolean(NULL_NAME) && bleDevice.device.name == null) {
            return
        }

        rssi = -getInt(RSSI, 100)
        filterDeviceList()
        if (bleDevice.rssi < rssi) {
            return
        }

        //检查之前所添加的设备地址是否存在当前地址列表
        val address = bleDevice.device.address
        if (!addressList.contains(address)) {
            addressList.add(address)
            mList.add(bleDevice)
        }
        //无设备UI展示
        binding.layNoEquipment.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
        //刷新列表适配器
        bleAdapter.notifyDataSetChanged()
    }

    /**
     * 过滤设备列表
     */
    private fun filterDeviceList() {
        if (mList.size > 0) {
            val mIterator = mList.iterator()
            while (mIterator.hasNext()) {
                val next = mIterator.next()
                if ((getBoolean(NULL_NAME) && next.device.name == null) || next.rssi < rssi) {
                    addressList.remove(next.device.address)
                    mIterator.remove()
                }
            }
            bleAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Android版本
     */
    private fun checkAndroidVersion() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermission() else openBluetooth()

    /**
     * 请求权限
     */
    private fun requestPermission() =
        PermissionX.init(this).permissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .request { allGranted, _, _ -> if (allGranted) openBluetooth() else showMsg() }

    /**
     * 打开蓝牙
     */
    private fun openBluetooth() = defaultAdapter.let {
        if (it.isEnabled) showMsg("蓝牙已打开，可以开始扫描设备了") else activityResult.launch(
            Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        )
    }

    /**
     * 扫描蓝牙
     */
    private fun scan() {
        if (!defaultAdapter.isEnabled) {
            showMsg("蓝牙未打开");return
        }
        if (isScanning) {
            showMsg("正在扫描中...");return
        }
        isScanning = true
        addressList.clear()
        mList.clear()
        BluetoothLeScannerCompat.getScanner().startScan(scanCallback)
        binding.progressBar.visibility = View.VISIBLE
        binding.fabAdd.text = "扫描中"
    }

    /**
     * 停止扫描
     */
    private fun stopScan() {
        if (!defaultAdapter.isEnabled) {
            showMsg("蓝牙未打开");return
        }
        if (isScanning) {
            isScanning = false
            BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
            binding.progressBar.visibility = View.INVISIBLE
            binding.fabAdd.text = "开始扫描"
        }
    }


    /**
     * Toast提示
     */
    private fun showMsg(msg: String = "权限未通过") =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

}

