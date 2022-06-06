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
    private var lossRateN :Int = 0
    @Volatile
    private var lossRateBase :Int = DEFAULT_BLOCK_SIZE
    private var lossRate :Double = 0.0

    private var cur = 0
    private var arrivalDeltaTime = 0
    private var sendDeltaTime = 0

    private var socket: UdpSocket = UdpSocket(UdpSocket.DEFAULT_PORT+1, false)
    private var reportPacketEncode = ReportPacketEncode()

    fun getDestAddressSetter(): SetDestAddress {
        return socket
    }

    override fun run() { //反馈控制信息

        var reportData = reportPacketEncode.makeReportPacket(delta, lossRateN, lossRateBase, lastSq)

        socket.sendPacket(reportData, reportData.size)
    }

    fun count(sq:Int, ts:Int, arrTs:Int, len:Int) {
        sqQueue.addLast(sq)
        tsQueue.addLast(ts)
        arrTsQueue.addLast(arrTs)
        lenQueue.addLast(len)
        dataSizeSum += len
        queueSize++

        if (queueSize > blockSize) { //满足统计窗口大小，计算统计信息 （实时计算型，考虑转阶段统计）
            /** 更新信息 **/
            /** 丢包率 **/
            sqQueue.removeFirst() //先出队 保证窗口大小 再进行统计
            lossRateN = sqQueue.last - sqQueue.first - blockSize + 1
            lossRate = lossRateN * 1.0 / blockSize

            /** 时间戳队列处理 **/
            tsQueue.removeFirst() //发送时间戳
            arrTsQueue.removeFirst() //接收时间戳

            /** 窗口中的总数据量 **/
            dataSizeSum -= lenQueue.first //数据总量
            lenQueue.removeFirst()

            queueSize--
        }
        /** 梯度时延计算 **/ /** 带宽估计 **/
        cur = (cur+1) % blockSize

        if(cur == blockSize-1) { //一个 block size 算一次
            /** 梯度时延 **/
            sendDeltaTime = tsQueue.last - tsQueue.first  //ti
            arrivalDeltaTime = arrTsQueue.last - arrTsQueue.first  //Ti
            delta = (sendDeltaTime - arrivalDeltaTime)  //Δ = Ti - ti
            /** 计算接收速率 **/
            var bps = dataSizeSum / arrivalDeltaTime // B/Ti
            lastSq = sq
            Log.i(TAG, " ---- 此刻序号 sq = $sq  梯度时延 delta = $delta  阶段接收速率 bps = $bps  ----  ")
            //TODO 接收端预处理 或发送时计算减少计算压力
        }


    }

    /**       =============有问题待修改==================               **/
    /** 到达时间滤波器  **/
    private fun arrivalFilter(ts:Int, arrTs:Int){


    }


    init {
        val timer = Timer()
        timer.schedule(this, 0, 5000)
    }

    companion object {
        private val TAG = DataStatics::class.java.simpleName
        const val DEFAULT_BLOCK_SIZE = 50
    }


}