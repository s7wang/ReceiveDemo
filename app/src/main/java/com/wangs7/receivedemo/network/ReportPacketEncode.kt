package com.wangs7.receivedemo.network

import android.util.Log

import com.wangs7.receivedemo.network.CalculateUtil.intToByte
import com.wangs7.receivedemo.network.CalculateUtil.memset

import java.nio.ByteBuffer
import kotlin.experimental.or

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/20 8:59
 **/
class ReportPacketEncode {
    private val reportBuffer = ByteArray(REPORT_BUFFER_SIZE)
    private var sequenceNumber = 0

    /**
     *  Report packet header
     *  Bit offset      0-1	        2	    3	    4-7	    8	9-15	    16-31
     *  0			    v=1                                            Sequence Number      31      4B
     *  32			                    Timestamp			                                63      8B
     *  64			    			Delta Timestamp			                                95      12B
     *  96                          Lost Rate n                                             127     16B
     *  128                         Lost Rate base                                          159     20B
     * */

    init {
        memset(reportBuffer, 0, REPORT_BUFFER_SIZE)
        reportBuffer[0] = reportBuffer[0] or 0x80.toByte()
        /** 填充其他信息 **/

    }

    /** 打包数据 **/
    fun makeReportPacket(deltaTs:Int, lostRate_n:Int, lostRate_base: Int, srcSq: Int):ByteArray {
        sequenceNumber = (sequenceNumber +1) % SHORT_MAX
        var report = ByteArray(REPORT_BUFFER_SIZE)
        memset(report, 0, REPORT_BUFFER_SIZE)
        /** v =  **/
        report[0] = report[0] or 0x40.toByte()

        var buffer = ByteBuffer.allocate(REPORT_BUFFER_SIZE)

//        System.arraycopy(intToByte(sequenceNumber), 0, report, 2, 2) //send[2]和send[3]为序列号，共两位
//        run {
//            // java默认的网络字节序是大端字节序（无论在什么平台上），因为windows为小字节序，所以必须倒序
//            var temp: Byte = report[3]
//            report[3] = report[2]
//            report[2] = temp
//        }
        var sq = ByteBuffer.allocate(2)
        sq.putShort(sequenceNumber.toShort())
        report[2] = sq[0]
        report[3] = sq[1]

        var dt = ByteBuffer.allocate(4)
        dt.putInt(deltaTs)
        report[8] = dt[0]
        report[9] = dt[1]
        report[10] = dt[2]
        report[11] = dt[3]

        var ln = ByteBuffer.allocate(4)
        ln.putInt(lostRate_n)
        report[12] = ln[0]
        report[13] = ln[1]
        report[14] = ln[2]
        report[15] = ln[3]

        var lb = ByteBuffer.allocate(4)
        lb.putInt(lostRate_base)
        report[16] = lb[0]
        report[17] = lb[1]
        report[18] = lb[2]
        report[19] = lb[3]

        var rsq = ByteBuffer.allocate(2)
        rsq.putShort(srcSq.toShort())
        report[20] = rsq[0]
        report[21] = rsq[1]


        return report
    }

    companion object {
        val TAG: String = ReportPacketEncode::class.java.simpleName
        const val REPORT_BUFFER_SIZE = 64
        const val SHORT_MAX = 65536
    }

}