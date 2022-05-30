package com.wangs7.receivedemo.preview

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.wangs7.receivedemo.codec.VideoDecoder
import com.wangs7.receivedemo.network.RtpReceiver

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/12 17:29
 **/
class ShowTextureView (context: Context?, attrs: AttributeSet?) : TextureView(
    context!!, attrs
), TextureView.SurfaceTextureListener {

    private lateinit var rtpReceiver:RtpReceiver
    private lateinit var videoDecoder: VideoDecoder

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable")

        videoDecoder.start(Surface(surface))
        rtpReceiver.start()
        var t = Thread{
            while (true) {
                var data = rtpReceiver.getRecVideoData()
                if( data == null) {
                    Thread.sleep(0, 10)
                    continue
                }
                videoDecoder.offerDecoder(data, data.size)
            }
        }
        t.priority = 10
        t.start()

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        //TODO("Not yet implemented")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        //videoDecoder.start(Surface(surface))
    }


    init {
        surfaceTextureListener = this
        rtpReceiver = RtpReceiver()
        videoDecoder = VideoDecoder()
        videoDecoder.setSize(960, 1280)
        videoDecoder.setBitRate(500_000)
        videoDecoder.setFrameRate(25)
        videoDecoder.setting()

    }

    companion object {
        val TAG: String = ShowTextureView::class.java.simpleName
    }
}