package com.wangs7.receivedemo.controller

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/23 8:51
 **/
interface ControlInfo {
    fun sendControlInfo(srcSq: Short, deltaTs:Int, lostRate:Double)
}