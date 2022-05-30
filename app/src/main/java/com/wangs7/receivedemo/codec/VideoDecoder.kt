package com.wangs7.receivedemo.codec

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/3/23 10:06
 **/
class VideoDecoder : SurfaceInfo{

    private var videoWidth: Int = DEFAULT_WIDTH
    private var videoHeight:Int = DEFAULT_HEIGHT
    private var frameRate: Int = DEFAULT_FRAME_RATE
    private var bitRate: Int = DEFAULT_BIT_RATE
    private var iFrequency: Int = DEFAULT_I_FREQUENCY

    private var mediaCodec: MediaCodec? = null
    private var mediaFormat: MediaFormat? = null
    private var surface: Surface? = null
    private var isGetSurface: Boolean = false

    private var mInfo: ByteArray? = null
    private var info: ByteArray? = null
    private var isRun: Boolean = false


    fun setSize(width:Int, height:Int) {
        videoWidth = width
        videoHeight = height
    }

    fun setFrameRate(newFrameRate:Int) {
        frameRate = newFrameRate
    }
    fun setBitRate(newBitRate: Int) {
        bitRate = newBitRate
    }

    fun setting() {

        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, videoWidth, videoHeight)
        mediaFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        mediaFormat!!.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
//        mediaFormat!!.setInteger(
//            MediaFormat.KEY_COLOR_FORMAT,
//            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
//        )
        mediaFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrequency)
        mediaFormat!!.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps))
        mediaFormat!!.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps))
        /**
         * CQ 表示完全不控制码率，尽最大可能保证图像质量, 质量要求高、不在乎带宽、
         * 解码器支持码率剧烈波动的情况下，可以选择这种策略；
         * CBR 表示编码器会尽量把输出码率控制为设定值，输出码率会在一定范围内波动，
         * 对于小幅晃动，方块效应会有所改善，但对剧烈晃动仍无能为力；连续调低码率则会导致码率急剧下降，
         * 如果无法接受这个问题，那 VBR 就不是好的选择；
         * VBR 表示编码器会根据图像内容的复杂度（实际上是帧间变化量的大小）来动态调整输出码率，
         * 图像复杂则码率高，图像简单则码率低，优点是稳定可控，这样对实时性的保证有帮助。
         */
        mediaFormat!!.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)
    }

    override fun setSurface(surface: Surface) {
        this.surface = surface
        isGetSurface = true
    }

    fun start() {
        if (surface != null) {
            start(surface)
        }
        else {
            Log.e(TAG, "surface == null")
        }
    }

    fun start(surface: Surface?) {
        try{
            mediaCodec!!.configure(mediaFormat, surface, null, 0)
            mediaCodec!!.start()
        }catch (e:Exception) {
            e.printStackTrace()
        }

        isRun = true
    }


     fun offerDecoder(input: ByteArray, length: Int) {
        Log.d(TAG, "offerDecoder: ")
        try {
            val inputBuffers = mediaCodec!!.inputBuffers
            val inputBufferIndex = mediaCodec!!.dequeueInputBuffer(0)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                try {
                    inputBuffer.put(input, 0, length)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mediaCodec!!.queueInputBuffer(inputBufferIndex, 0, length, 0, 0)
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            while (outputBufferIndex >= 0) {
                //If a valid surface was specified when configuring the codec,
                //passing true renders this output buffer to the surface.
                mediaCodec!!.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    init {
        Log.d(TAG, "VideoDecoder init.")
        try {
            mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE)
        } catch (e: IOException) {
            e.printStackTrace()
        }

//        videoEncoderHandlerThread.start()
//        videoEncoderHandler = Handler(videoEncoderHandlerThread.looper)
    }

    companion object {
        private val TAG = VideoDecoder::class.java.simpleName
        private const val MIME_TYPE = "video/avc"
        const val DEFAULT_WIDTH = 480
        const val DEFAULT_HEIGHT = 640
        const val DEFAULT_FRAME_RATE = 25
        const val DEFAULT_BIT_RATE = 500_000
        const val DEFAULT_I_FREQUENCY = 2
        private val header_sps = byteArrayOf(
            0, 0, 0, 1,
            103, 66, 0, 41, -115, -115, 64, 80, 30, -48, 15, 8, -124, 83, -128
        )
        private val header_pps = byteArrayOf(0, 0, 0, 1, 104, -54, 67, -56)

        private const val CACHE_BUFFER_SIZE = 32


    }
}