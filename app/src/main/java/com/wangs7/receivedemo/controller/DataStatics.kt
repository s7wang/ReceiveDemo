package com.wangs7.receivedemo.controller

import android.util.Log
import com.wangs7.receivedemo.network.ReportPacketEncode
import com.wangs7.receivedemo.network.SetDestAddress
import com.wangs7.receivedemo.network.UdpSocket
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
    private var blockSize : Int = DEFAULT_BLOCK_SIZE
    private var dataSizeSum : Int = 0
    private var deltaTime : Int = 0
    private var delta : Int = 0


    @Volatile
    private var lastSq: Int = 0

    private var sqQueue = LinkedList<Int>()
    private var tsQueue = LinkedList<Int>()
    private var arrTsQueue = LinkedList<Int>()
    private var lenQueue = LinkedList<Int>()
    private var queueSize = 0;
    @Volatile
    private var lossRate_n :Int = 0
    @Volatile
    private var lossRate_base :Int = DEFAULT_BLOCK_SIZE
    private var lossRate :Double = 0.0

    private var arrivalFilterStep:Int = 10
    private var cur = 0
    private var arrivalDeltaTime = 0
    private var sendDeltaTime = 0

    private var socket: UdpSocket = UdpSocket(UdpSocket.DEFAULT_PORT+1, false)
    private var reportPacketEncode = ReportPacketEncode()

    private var kalmanInfo = KalmanInfo()
    private class KalmanInfo {
        var filterValue:Double = 0.0 //滤波后的值
        var kalmanGain:Double = 1.0 //Kalamn增益
        var a:Double = 1.0 //状态矩阵
        var h:Double = 1.0 //观测矩阵
        var q:Double = 0.1 //状态矩阵的方差
        var r:Double = 0.5 //观测矩阵的方差
        var p:Double = 0.01 //预测误差
        var b:Double = 0.1
        var u:Double = 0.0

    }
    fun getDestAddressSetter(): SetDestAddress {
        return socket
    }

    override fun run() {

        var reportData = reportPacketEncode.makeReportPacket(delta, lossRate_n, lossRate_base, lastSq)

        socket.sendPacket(reportData, reportData.size)
    }

    fun count(sq:Int, ts:Int, arrTs:Int, len:Int) {
        sqQueue.addLast(sq)
        tsQueue.addLast(ts)
        arrTsQueue.addLast(arrTs)
        lenQueue.addLast(len)
        dataSizeSum += len
        queueSize++


        if (queueSize > blockSize) {
            /** 更新信息 **/
            sqQueue.removeFirst()
            lossRate_n = sqQueue.last - sqQueue.first - blockSize + 1
            lossRate = lossRate_n * 1.0 / blockSize

            tsQueue.removeFirst()
            deltaTime = tsQueue.last - tsQueue.first //rtp时间戳

            arrTsQueue.removeFirst() //接收时间

            dataSizeSum -= lenQueue.first //数据总量
            lenQueue.removeFirst()

            queueSize--


        }
        /** 梯度时延计算 **/ /** 带宽估计 **/
        cur = (cur+1) % arrivalFilterStep
        if(cur == arrivalFilterStep-1) {
            arrivalDeltaTime = arrTsQueue.last - arrTsQueue.first
            sendDeltaTime = tsQueue.last - tsQueue.first
            //var out = arrivalFilter(sendDeltaTime, arrivalDeltaTime)
            delta = (sendDeltaTime - arrivalDeltaTime)
            var bps = dataSizeSum / arrivalDeltaTime
            lastSq = sq
            Log.i(TAG, " ----  arrivalDeltaTime = $arrivalDeltaTime     bps = $bps  ----  ")
        }

        if (queueSize == blockSize) {

            Log.e(TAG, "++++ dataSizeSum = $dataSizeSum       delta = $delta      lastsq = $lastSq ++++  ")

        }


    }

    /**       =============有问题待修改==================               **/
    /** 到达时间滤波器  **/
    private fun arrivalFilter(ts:Int, arrTs:Int):Double {
        var delta = (arrTs - ts) * 1.0
        var out = kalmanFilter(delta)
        Log.i(TAG, " ++++++  delta = $delta  out = $out  ++++++  ")
        return out
    }

    private fun kalmanFilter(input:Double):Double {
        var predictValue = kalmanInfo.a * kalmanInfo.filterValue + kalmanInfo.b * kalmanInfo.u
        kalmanInfo.p = kalmanInfo.a * kalmanInfo.a * kalmanInfo.p +kalmanInfo.q
        kalmanInfo.kalmanGain = kalmanInfo.p * kalmanInfo.h / (kalmanInfo.p * kalmanInfo.h * kalmanInfo.h + kalmanInfo.r)

        kalmanInfo.filterValue = predictValue + (input - predictValue) * kalmanInfo.kalmanGain
        kalmanInfo.p = (1 - kalmanInfo.kalmanGain * kalmanInfo.h) * kalmanInfo.p
        return kalmanInfo.filterValue
    }

    init {
        val timer = Timer()
        timer.schedule(this, 0, 5000)
    }

    companion object {
        private val TAG = DataStatics::class.java.simpleName
        const val DEFAULT_BLOCK_SIZE = 10
    }


}