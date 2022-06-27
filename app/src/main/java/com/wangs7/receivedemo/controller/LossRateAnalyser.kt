package com.wangs7.receivedemo.controller

import com.wangs7.receivedemo.network.RtpPacketDecode
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/6/21 9:18
 **/
class LossRateAnalyser(time:Int): TimerTask() {

    private var lossBase = 1
    private var loss = 0
    private var lossRate = 0.0
    private val lock = ReentrantLock()

    private var lastSq = 0
    fun analyse(sq:Int) {

        if (sq == 1 || lastSq - sq > RtpPacketDecode.SHORT_MAX/4*3) {
            lastSq = 0
        }

        lock.lock()
        if (lastSq+1 != sq) {
            loss += (sq - lastSq - 1)
            lossBase += (sq - lastSq)
        } else {
            lossBase++
        }
        lastSq = sq

        lock.unlock()

    }

    fun start() {
        val timer = Timer()
        timer.schedule(this, 0, 1000)
    }

    fun getLoss():Int {
        return loss
    }

    fun getLossBase():Int {
        return lossBase
    }

    companion object {
        private val TAG = LossRateAnalyser::class.java.simpleName
    }

    override fun run() {
        lock.lock()
        lossRate = loss * 1.0 / lossBase
        loss = 0
        lossBase = 1
        lock.unlock()
    }
}