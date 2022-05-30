package com.wangs7.receivedemo.controller

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.wangs7.receivedemo.network.ReportPacketEncode
import com.wangs7.receivedemo.network.SetDestAddress
import com.wangs7.receivedemo.network.UdpSocket
import java.net.InetAddress

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/16 16:22
 **/
class RcvController {

    private var statics:DataStatics = DataStatics()
    private var staticThread:HandlerThread = HandlerThread("StaticThread")
    private var staticHandler:Handler
    fun getDestAddressSetter():SetDestAddress {
        return statics.getDestAddressSetter()
    }
    fun staticsMessage(sq:Int, ts:Int, arrTs:Int, len:Int) {
        var message = Message()
        var bundle = Bundle()
        bundle.putInt(SQ_NUM, sq)
        bundle.putInt(TIMESTAMP, ts)
        bundle.putInt(ARR_TIMESTAMP, arrTs)
        bundle.putInt(DATA_SIZE, len)
        message.data = bundle
        staticHandler.handleMessage(message)

    }

    init {
        staticThread.start()

        /** 统计信息处理 **/
        staticHandler = object :Handler(staticThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                var bundle = msg.data
                var sq:Int = bundle.get(SQ_NUM) as Int
                var ts:Int = bundle.get(TIMESTAMP) as Int
                var arrTs:Int = bundle.get(ARR_TIMESTAMP) as Int
                var len:Int = bundle.get(DATA_SIZE) as Int

                statics.count(sq, ts, arrTs, len)


            }
        }

    }

    companion object {
        private val TAG = RcvController::class.java.simpleName
        const val SQ_NUM = "SQ_NUM"
        const val TIMESTAMP = "TIMESTAMP"
        const val ARR_TIMESTAMP = "ARR_TIMESTAMP"
        const val DATA_SIZE = "DATA_SIZE"
    }
}