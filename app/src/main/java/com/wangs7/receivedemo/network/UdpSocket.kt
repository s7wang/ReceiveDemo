package com.wangs7.receivedemo.network

import android.util.Log
import com.wangs7.receivedemo.controller.DataStatics
import java.io.IOException
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/3/29 1:27
 **/
class UdpSocket(localPort_:Int, broadcast: Boolean):SetDestAddress {
    private var socket: DatagramSocket? = null
    private var inetAddress: InetAddress? = null
    private var receiveIp: InetAddress? = null
    private var localPort = 0
    private var destPort = DEFAULT_PORT

    private var destAddressSetter : SetDestAddress? = null


    private var sendPacket: DatagramPacket? = null

    fun setDestAddressSetter(setter : SetDestAddress) {
        destAddressSetter = setter
    }
    fun setAddress(IP: String?, desPort_: Int) {
        inetAddress = InetAddress.getByName(IP)
        destPort = desPort_
    }
    override fun setAddress(IP: InetAddress, port: Int) {
        inetAddress = IP
        destPort = port
    }

    fun sendPacket(data:ByteArray, size:Int) {
        try {
            //addTimeStamp(data)
            var timeStamp = ByteBuffer.allocate(4)
            timeStamp.putInt((System.currentTimeMillis()%Int.MAX_VALUE).toInt())
            data[4] = timeStamp[0]
            data[5] = timeStamp[1]
            data[6] = timeStamp[2]
            data[7] = timeStamp[3]

            if (inetAddress == null) {
                Log.e(TAG, "inetAddress == null")
                return
            }

            sendPacket = DatagramPacket(data, 0, size, inetAddress, destPort)
            if (socket == null) {
//                socket = DatagramSocket(localPort)
//                socket!!.broadcast = false
                Log.e(TAG,"Socket == null")
            }
            socket!!.send(sendPacket)
            Log.e(TAG, "sendPacket+++++++++++++++++++++++++++++++++++++++++")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun receivePacket(): ByteArray? {
        val data = ByteArray(RECEIVE_BUFFER_SIZE)
        var size: Int
        var datagramPacket = DatagramPacket(data, data.size)
        if (socket != null) {
            try {
                socket!!.receive(datagramPacket)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var ip = datagramPacket.address
        if (!ip.equals(receiveIp)) {
            receiveIp = ip
            Log.e(TAG, "ip = ${ip.hostAddress}")
            destAddressSetter?.setAddress(ip, DEFAULT_PORT+1)
        }

        size = datagramPacket.length
        return Arrays.copyOf(datagramPacket.data, size)
    }

    fun close() {
        socket!!.close()
    }

    init {
        try {
            localPort = localPort_
            socket = DatagramSocket(localPort)
            socket!!.broadcast = broadcast
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = UdpSocket::class.java.simpleName

        const val RECEIVE_BUFFER_SIZE = 80000
        const val DEFAULT_PORT = 5004
    }


}