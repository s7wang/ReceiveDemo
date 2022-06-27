package com.wangs7.receivedemo.controller

import android.util.Log
import com.wangs7.receivedemo.network.RtpReceiver

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/6/21 10:40
 **/
class TimeDelayAnalyser {

    private var sendDeltaTime = 0
    private var receiveDeltaTime = 0
    private var delta = 0

    private var s0 = 0
    private var r0 = 0

    private var blockSize = 50

    private var count = 0

    private var delayFilter = DelayFilter()

    fun analyse(sendTs:Int, receiveTs:Int) {
        if (count == 0) {
            s0 = sendTs
            r0 = receiveTs
        }
        if(count == blockSize - 1) {
            sendDeltaTime = sendTs - s0
            receiveDeltaTime = receiveTs - r0
            delta = receiveDeltaTime -sendDeltaTime
            /** 待完善 **/ //梯度延时处理
            delayFilter.filter(delta)
        }


        count = (count + 1) % blockSize
    }

    companion object {
        private val TAG = TimeDelayAnalyser::class.java.simpleName
    }
}