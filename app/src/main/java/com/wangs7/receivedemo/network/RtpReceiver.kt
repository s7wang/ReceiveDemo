package com.wangs7.receivedemo.network

import android.util.Log
import com.wangs7.receivedemo.controller.RcvController
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.PriorityBlockingQueue

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
    private val bufferSize = 64

    private val capacity = 512;


    private val queue: PriorityBlockingQueue<RtpPacket> = PriorityBlockingQueue<RtpPacket>(capacity
    ) { o1, o2 ->
        if (o1 != null && o2 != null) {
            if (o1.ts == o2.ts) {
                if (o1.sn - o2.sn < RtpPacketDecode.SHORT_MAX / 4 * 3)
                    o1.sn - o2.sn
                else
                    o2.sn - o1.sn
            } else {
                o1.ts - o2.ts
            }
        } else {
            0
        }
    };


    private var isRunning = false

    private val receiveThread:Runnable = Runnable {

        var controller = RcvController()
        socket?.setDestAddressSetter(controller.getDestAddressSetter())
        var lastSq: Int = 0
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
                /** 序列号 时间戳 信息 **/  /** 乱序检测 **/
                var packet:RtpPacket = RtpPacket(data)
                controller.staticsMessage(packet.sn, packet.ts, (System.currentTimeMillis()%Int.MAX_VALUE).toInt(), packet.data.size)
                //Log.e(TAG, " packet.sn = ${packet.sn}, packet.ts = ${packet.ts}, d.data.size = ${packet.data.size}")
                queue.put(packet) /** 优先队列处理乱序 **/
                //controller.staticsMessage(sn, ts, (System.currentTimeMillis()%Int.MAX_VALUE).toInt(), data.size)
                /** 解RTP包 **/
                if (queue.size > bufferSize) {
                    var d = queue.poll()
                    //Log.e(TAG, " d.sn = ${d.sn}, d.ts = ${d.ts}, d.data.size = ${d.data.size}")
                    if(d.sn == 1)
                        lastSq = 0
                    if( lastSq >= d.sn
                        && (lastSq - d.sn < RtpPacketDecode.SHORT_MAX/4*3)
                    ) {
                        Log.e(TAG, "------------- error packet --------------------------------------------------------------------------")

                        if (rtpPacketDecode?.isBufferEmpty() == false) {
                            //设置状态 等待I帧
                            //发送请求I帧指令
                        }
                        rtpPacketDecode?.clearBuffer()
                        continue

                    } else {
                        lastSq = d.sn
                    }
                    controller.lossAnalyse(lastSq)

                    var rece = rtpPacketDecode?.rtp2h264(d.data, d.data.size)
                    /**可以对解码帧进行统计**/
                    /** 解码视频 **/
                    if (rece != null) {
                        receiveQueue.offer(rece)
                    }
                }

//                var rece = rtpPacketDecode?.rtp2h264(data, data.size)
//                /**可以对解码帧进行统计**/
//                /** 解码视频 **/
//                if (rece != null) {
//                    receiveQueue.offer(rece)
//                }
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
//        var c = Thread(decodeThread)
//        c.priority = 9
//        c.start()
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
        const val NORMAL = 1
        const val I_FRAME = 2
        const val P_FRAME = 3

    }
}