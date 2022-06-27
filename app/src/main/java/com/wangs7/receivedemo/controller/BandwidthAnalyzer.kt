package com.wangs7.receivedemo.controller

import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/6/20 17:13
 **/
class BandwidthAnalyzer(time: Int) : TimerTask(){
    private var blockTime = time
    private var receiveDataSize = 0L
    @Volatile
    private var bandwidth = 0
    private val lock = ReentrantLock()

    fun analyse(dataSize: Int):Int {
        lock.lock()
        receiveDataSize += dataSize
        lock.unlock()
        return 0
    }

    fun getBandwidth():Int {
        return bandwidth
    }

    override fun run() {
        lock.lock()
        bandwidth = (receiveDataSize / 1000).toInt()
        receiveDataSize = 0
        lock.unlock()

    }

    fun start() {
        val timer = Timer()
        timer.schedule(this, 0, 1000)
    }

    companion object {
        private val TAG = BandwidthAnalyzer::class.java.simpleName
    }
}