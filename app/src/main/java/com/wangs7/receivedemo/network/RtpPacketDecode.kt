package com.wangs7.receivedemo.network

import com.wangs7.receivedemo.network.CalculateUtil.byteToInt
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/4/6 10:01
 */
class RtpPacketDecode(width: Int, height: Int) {
    private val h264Buffer: ByteArray
    private var h264Len = 0
    private var h264Pos = 0

    fun isBufferEmpty():Boolean {
        return h264Len == 0
    }

    fun clearBuffer() {
        h264Len = 0
        h264Pos = -1
    }
    /**
     * RTP解包H264
     *
     * @param rtpData
     * @return
     */

    fun rtp2h264(rtpData: ByteArray, rtpLen: Int): ByteArray? {
        /**
         * RTP packet header
         * Bit offset     0-1	    2	    3	    4-7	    8	9-15	    16-31
         * 0			Version	    P	    X	    CC	    M	PT	    Sequence Number     31
         * 32			Timestamp									                        63
         * 64			SSRC identifier								                        95
         */
        /** FU-Header长度为12字节 **/
        var rtpHeaderLen = 12
        val extension: Int = rtpData[0].toInt() and (1 shl 4) // X: 扩展为是否为1
        if (extension > 0) {
            // 计算扩展头的长度
            val extLen: Int =
                (rtpData[12].toInt() shl 24) + (rtpData[13].toInt() shl 16) + (rtpData[14].toInt() shl 8) + rtpData[15]
            rtpHeaderLen += (extLen + 1) * 4
        }
        // 解析FU-indicator
        val indicatorType =
            (byteToInt(rtpData[rtpHeaderLen]) and 0x1f).toByte() // 取出low 5 bit 则为FU-indicator type
        val nri = (byteToInt(rtpData[rtpHeaderLen]) shr 5 and 0x03).toByte() // 取出h2bit and h3bit
        val f = (byteToInt(rtpData[rtpHeaderLen]) shr 7).toByte() // 取出h1bit
        val h264NalHeader: Byte
        val fuHeader: Byte
        if (indicatorType.toInt() == 28) {  // FU-A
            fuHeader = rtpData[rtpHeaderLen + 1]
            val s = (rtpData[rtpHeaderLen + 1] and 0x80.toByte())
            val e = (rtpData[rtpHeaderLen + 1] and 0x40.toByte())
            if (e.toInt() == 64) {   // end of fu-a
                //ZOLogUtil.d("RtpParser", "end of fu-a.....;;;");
                val temp = ByteArray(rtpLen - (rtpHeaderLen + 2))
                System.arraycopy(rtpData, rtpHeaderLen + 2, temp, 0, temp.size)
                writeData2Buffer(temp, temp.size)
                if (h264Pos >= 0) {
                    h264Pos = -1
                    if (h264Len > 0) {
                        val h264Data = ByteArray(h264Len)
                        System.arraycopy(h264Buffer, 0, h264Data, 0, h264Len)
                        h264Len = 0
                        return h264Data
                    }
                }
            } else if (s.toInt() == -128) { // start of fu-a
                h264Pos = 0 // 指针归0
                writeData2Buffer(start_code, 4) // 写入H264起始码
                h264NalHeader = (fuHeader and 0x1f or (nri.toInt() shl 5).toByte() or (f.toInt() shl 7).toByte())
                writeData2Buffer(byteArrayOf(h264NalHeader), 1)
                val temp = ByteArray(rtpLen - (rtpHeaderLen + 2))
                System.arraycopy(rtpData, rtpHeaderLen + 2, temp, 0, temp.size) // 负载数据
                writeData2Buffer(temp, temp.size)
            } else {
                val temp = ByteArray(rtpLen - (rtpHeaderLen + 2))
                System.arraycopy(rtpData, rtpHeaderLen + 2, temp, 0, temp.size)
                writeData2Buffer(temp, temp.size)
            }
        } else { // nalu
            h264Pos = 0
            writeData2Buffer(start_code, 4)
            val temp = ByteArray(rtpLen - rtpHeaderLen)
            System.arraycopy(rtpData, rtpHeaderLen, temp, 0, temp.size)
            writeData2Buffer(temp, temp.size)
            if (h264Pos >= 0) {
                h264Pos = -1
                if (h264Len > 0) {
                    val h264Data = ByteArray(h264Len)
                    System.arraycopy(h264Buffer, 0, h264Data, 0, h264Len)
                    h264Len = 0
                    return h264Data
                }
            }
        }
        return null
    }

    private fun writeData2Buffer(data: ByteArray, len: Int) {
        if (h264Pos >= 0) {
            System.arraycopy(data, 0, h264Buffer, h264Pos, len)
            h264Pos += len
            h264Len += len
        }
    }

    //计算h264大小
    private fun getYuvBuffer(width: Int, height: Int): Int {
        // stride = ALIGN(width, 16)
        val stride = ceil(width / 16.0).toInt() * 16
        // y_size = stride * height
        val ySize = stride * height
        // c_stride = ALIGN(stride/2, 16)
        val cStride = ceil(width / 32.0).toInt() * 16
        // c_size = c_stride * height/2
        val cSize = cStride * height / 2
        // size = y_size + c_size * 2
        return ySize + cSize * 2
    }
    
    companion object {
        private val start_code = byteArrayOf(0, 0, 0, 1) // h264 start code
        const val SHORT_MAX = 65536
    }

    //传入视频的分辨率
    init {
        h264Buffer = ByteArray(getYuvBuffer(width, height))
    }
}