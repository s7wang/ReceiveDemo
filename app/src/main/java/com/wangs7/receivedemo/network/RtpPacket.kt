package com.wangs7.receivedemo.network

import android.util.Log
import java.nio.ByteBuffer

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/6/15 9:02
 **/
class RtpPacket(packet:ByteArray) {
    var data = packet
    var sn:Int
    var ts:Int

    init {
        var seqNum: ByteBuffer = ByteBuffer.allocate(4)
        seqNum.put(data.copyOfRange(2, 4)) // 包序
        sn = seqNum.getShort(0).toUShort().toInt()
        //Log.i(TAG,"=========== seq = ${sn}===========")

        var timeStamp: ByteBuffer = ByteBuffer.allocate(4)
        timeStamp.put(data.copyOfRange(4, 8)) // 时间戳
        ts = timeStamp.getInt( 0)
        //Log.i(TAG,"=========== TimeStamp = ${ts}===========")
    }
    companion object {
        val TAG: String = RtpPacket::class.java.simpleName
    }
}