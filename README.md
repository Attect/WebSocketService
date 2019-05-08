# WebSocketService
Android WebSocket Service实现

如果你困扰怎么在Android上使用WebSocket，此项目是你很好的研究对象

Demo可用作WebSocket测试工具

基于okhttp3

这是一个LocalService，并不支持运行在独立进程中

## 用法
导入此项目自己阅读app模块的代码

## 功能
 1. 这个Demo可以直接作为WebSocket测试工具使用
 1. 字符串数据内容即时收发
 1. 二进制数据内容即时收发
 1. 轻松的获取连接状态
 1. 可选的后台持续运行功能
 1. 自动重连
 
 ## 数据传递
 Service与其它安卓组件进行数据交换不是一件很方便的事情
 
 此项目使用 https://github.com/Attect/StaticViewModelStore 来实现在同一进程的App内，任意Activity/Fragment/Service及其它LifecycleOwner中进行数据的收发和控制这个Service
 
 ## 演示
 https://www.bilibili.com/video/av51729765/
