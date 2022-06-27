package com.wangs7.receivedemo.controller

import android.util.Log
import com.wangs7.receivedemo.network.SetDestAddress
import com.wangs7.receivedemo.network.UdpSocket
import org.json.JSONObject
import java.util.*

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/16 16:21
 **/
class DataStatics:TimerTask() {

    private var socket: UdpSocket = UdpSocket(UdpSocket.DEFAULT_PORT+1, false)

    /** 新版信息统计 **/
    private var inver = InversionAnalyser(DEFAULT_BLOCK_SIZE)
    private var bandwidthAnalyzer = BandwidthAnalyzer(0)
    private var lossRateAnalyser = LossRateAnalyser(0)
    private var timeDelayAnalyser = TimeDelayAnalyser()

    /** 反馈结果 **/
    private var inverRes = 0

    fun getDestAddressSetter(): SetDestAddress {
        return socket
    }

    override fun run() { //反馈控制信息

//        var reportData = reportPacketEncode.makeReportPacket(delta, lossRateN, lossRateBase, lastSq)
//        Log.i(TAG,"delta = $delta, lossRateN = $lossRateN, lossRateBase = $lossRateBase, lastSq = $lastSq ")
//        socket.sendPacket(reportData, reportData.size)

        /** JSON 信息 **/
        var json = JSONObject()
//        json.put("test_int", 1)
//        json.put("test_str", "hello")

        json.put("inver_res", inverRes)
        json.put("bandwidth", bandwidthAnalyzer.getBandwidth())
        json.put("loss", lossRateAnalyser.getLoss())
        json.put("loss_base", lossRateAnalyser.getLossBase())

        var jsonStr = json.toString()
        var test = ByteArray(8+jsonStr.toByteArray().size)

        System.arraycopy(jsonStr.toByteArray(), 0, test, 8, jsonStr.toByteArray().size)
        socket.sendPacket(test, test.size)
        Log.e(TAG, "band = ${bandwidthAnalyzer.getBandwidth()} loss = ${lossRateAnalyser.getLoss()}  lossBase = ${lossRateAnalyser.getLossBase()}==================================================")

    }

    /** 新版统计 **/
    //乱序程度分析 接收带宽分析 时延分析
    fun count1(sq:Int, ts:Int, arrTs:Int, len:Int) {
        // 乱序程度分析
        inverRes = inver.analyse(sq)
        // 接收带宽分析
        bandwidthAnalyzer.analyse(len)
        // 时延分析
        timeDelayAnalyser.analyse(ts, arrTs)

    }
    //丢包率分析
    fun lossAnalyse(sq:Int) {
        lossRateAnalyser.analyse(sq)
    }

    /**       =============有问题待修改==================               **/
    /** 到达时间滤波器  **/
    private fun arrivalFilter(ts:Int, arrTs:Int){

    }


    init {
        val timer = Timer()
        timer.schedule(this, 0, 5000)
        bandwidthAnalyzer.start()
        lossRateAnalyser.start()
    }

    companion object {
        private val TAG = DataStatics::class.java.simpleName
        const val DEFAULT_BLOCK_SIZE = 50
    }


}