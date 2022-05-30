package com.wangs7.receivedemo.controller

import android.util.Log

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/4/19 9:07
 **/
class InterArrivalFilter {
    private var timeCurrentTimeMillis = System.currentTimeMillis()

    private var groupStartArrivalTime: Int = 0
    private var groupEndArrivalTime: Int = 0

    private var groupStartSendTime: Int = 0
    private var groupEndSendTime: Int = 0

    private var arrivalDeltaTime = 0.0
    private var sendDeltaTime = 0.0
    private var deltaT: Double= 0.0

    private var GROUP_SIZE = 5
    private var count = -1
    private var kalmanInfo = KalmanInfo()
    private class KalmanInfo {
        var filterValue:Double = 0.0 //滤波后的值
        var kalmanGain:Double = 0.0 //Kalamn增益
        var a:Double = 1.0 //状态矩阵
        var h:Double = 1.0 //观测矩阵
        var q:Double = 0.05 //状态矩阵的方差
        var r:Double = 0.1 //观测矩阵的方差
        var p:Double = 0.1 //预测误差
        var b:Double = 0.1
        var u:Double = 0.0

    }

    fun setGroupSize(newSize:Int) {
        GROUP_SIZE = newSize
    }

    fun bandwidthEstimation(dataSize: Int, sendTime:Int):Int? {
        if (count == -1) {
            groupStartArrivalTime = System.currentTimeMillis().toInt()
            groupStartSendTime = sendTime
        }
        if (count == GROUP_SIZE - 1) {
            groupEndArrivalTime = System.currentTimeMillis().toInt()
            groupEndSendTime = sendTime
            // 计算到达时间差
            arrivalDeltaTime = (groupEndArrivalTime - groupStartArrivalTime).toDouble()
            sendDeltaTime = (groupEndSendTime - groupStartSendTime).toDouble()

            groupStartArrivalTime = groupEndArrivalTime
            groupStartSendTime = groupEndSendTime
            deltaT = arrivalDeltaTime - sendDeltaTime

            Log.e(TAG, "deltaT = $deltaT    arrivalDeltaTime = $arrivalDeltaTime    sendDeltaTime = $sendDeltaTime    groupEndArrivalTime =  $groupEndArrivalTime"  )
            count = (count + 1) % GROUP_SIZE
            kalmanFilter(deltaT)


        }else{
            count = (count + 1) % GROUP_SIZE
        }

        return null
    }

    private fun bandwidthCalculate() {

    }

    private fun kalmanFilter(input:Double):Double {
        var predictValue = kalmanInfo.a * kalmanInfo.filterValue + kalmanInfo.b * kalmanInfo.u
        kalmanInfo.p = kalmanInfo.a * kalmanInfo.a * kalmanInfo.p +kalmanInfo.q
        kalmanInfo.kalmanGain = kalmanInfo.p * kalmanInfo.h / (kalmanInfo.p * kalmanInfo.h * kalmanInfo.h + kalmanInfo.r)

        kalmanInfo.filterValue = predictValue + (input - predictValue) * kalmanInfo.kalmanGain
        kalmanInfo.p = (1 - kalmanInfo.kalmanGain * kalmanInfo.h) * kalmanInfo.p
        return kalmanInfo.filterValue
    }



    companion object {
        private val TAG = InterArrivalFilter::class.java.simpleName
    }
}
