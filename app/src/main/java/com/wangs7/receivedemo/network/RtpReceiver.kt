package com.wangs7.receivedemo.network

import android.util.Log
import com.wangs7.receivedemo.controller.RcvController
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/12 17:32
 **/
class RtpReceiver {

    private var socket : UdpSocket? = null
    private var rtpPacketDecode:RtpPacketDecode? = null
    private val RECEIVE_QUEUE_SIZE = 256
    private val receiveQueue = ArrayBlockingQueue<ByteArray>(RECEIVE_QUEUE_SIZE)


    private var isRunning = false

    private val receiveThread:Runnable = Runnable {
        var lastSq: Int = 0
        var controller = RcvController()
        socket?.setDestAddressSetter(controller.getDestAddressSetter())
        while (isRunning) {
            if (socket == null) {
                Log.e(TAG, "receiveThread socket == null")
                break
            }
            var data = socket!!.receivePacket()

            if ( data != null &&
                (data[0].toInt() == -128 &&
                (data[1].toInt() == 96 || data[1].toInt() == -32) )
            ) {
                /** 序列号 时间戳 信息 **/
                var seqNum: ByteBuffer = ByteBuffer.allocate(4)
                seqNum.put(data.copyOfRange(2, 4)) // 包序
                var sn = seqNum.getShort(0).toUShort().toInt()
                Log.i(TAG,"=========== seq = ${sn}===========")

                var timeStamp: ByteBuffer = ByteBuffer.allocate(4)
                timeStamp.put(data.copyOfRange(4, 8)) // 时间戳
                var ts = timeStamp.getInt( 0)
                Log.i(TAG,"=========== TimeStamp = ${ts}===========")

                /** 乱序检测 **/
                if(sn == 1)
                    lastSq = 0
                if( lastSq >= sn
                    && (lastSq - sn < RtpPacketDecode.SHORT_MAX/4*3)
                ) {
                    Log.e(TAG, "------------- error packet --------------")
                    continue
                } else {
                    lastSq = sn
                }

                controller.staticsMessage(sn, ts, (System.currentTimeMillis()%Int.MAX_VALUE).toInt(), data.size)
                /** 解RTP包 **/
                var rece = rtpPacketDecode?.rtp2h264(data, data.size)
                /**可以对解码帧进行统计**/
                /** 解码视频 **/
                if (rece != null) {
                    receiveQueue.offer(rece)
                }
            }

        }


    }
    fun getRecVideoData(): ByteArray? {

        return receiveQueue.poll()
    }


    fun start() {
        isRunning = true
        var t = Thread(receiveThread)
        t.priority = 9
        t.start()
    }
    fun stop() {
        isRunning = false

    }


    init {
        socket = UdpSocket(UdpSocket.DEFAULT_PORT, false)
        rtpPacketDecode = RtpPacketDecode(1024, 1280)

    }

    companion object {
        val TAG: String = RtpReceiver::class.java.simpleName
    }
}