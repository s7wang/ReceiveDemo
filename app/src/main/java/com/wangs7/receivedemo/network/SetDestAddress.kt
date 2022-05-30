package com.wangs7.receivedemo.network

import java.net.InetAddress

/**
 * @version:
 * @author: wangs7__
 * @className:
 * @packageName:
 * @description:
 * @date: 2022/5/23 9:30
 **/
interface SetDestAddress {
    fun setAddress(IP: InetAddress, port:Int)
}