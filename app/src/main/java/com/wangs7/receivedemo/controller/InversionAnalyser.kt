package com.wangs7.receivedemo.controller

import android.util.Log
import com.wangs7.receivedemo.network.RtpPacketDecode

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/6/20 13:32
 **/
class InversionAnalyser(size: Int) {
    private var arraySize = size
    private var sqArray = IntArray(size)
    private var inversionArray = IntArray(size)

    private var head = 0
    private var tail = 0

    fun analyse(newSq:Int):Int {
        if (newSq == 1) {
            head = 0
            tail = 0
        }

        if(newSq > RtpPacketDecode.SHORT_MAX/4*3
            && (isEmpty() || sqArray[tail] < RtpPacketDecode.SHORT_MAX/4)) {
            return -1
        }
        inversionArray[tail] = 0


        if (isEmpty()) {
            sqArray[tail] = newSq

        } else if (isFull()) {
            var oldHead = sqArray[head]
            head = (head + 1) % arraySize
            sqArray[tail] = newSq
            var cur = head
            while (cur != tail) {
                if (oldHead > sqArray[cur])
                    inversionArray[cur]--
                if (newSq < sqArray[cur])
                    inversionArray[tail]++
                cur = (cur + 1) % arraySize
            }
        } else {
            sqArray[tail] = newSq
            var cur = head
            while (cur != tail) {
                if (newSq < sqArray[cur])
                    inversionArray[tail]++
                cur = (cur + 1) % arraySize
            }
        }

        tail = (tail +1) % arraySize
        //累加 inversionArray 即得乱序值
        var res = 0
        var cur = head
        while (cur != tail) {
            res += inversionArray[cur]
            cur = (cur + 1) % arraySize
        }
        return res
    }



    private fun isEmpty():Boolean {
        return head == tail
    }

    private fun isFull():Boolean {
        return (tail + 1) % arraySize == head
    }

    companion object {
        private val TAG = InversionAnalyser::class.java.simpleName
    }

}

