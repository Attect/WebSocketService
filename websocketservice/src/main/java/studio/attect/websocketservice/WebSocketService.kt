package studio.attect.websocketservice


import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Observer
import okhttp3.*
import okio.ByteString
import studio.attect.staticviewmodelstore.StaticViewModelLifecycleService
import studio.attect.staticviewmodelstore.StaticViewModelStore
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * WebSocketService
 * 是一个LocalService
 * 只支持启动一次，支持绑定前台通知
 * 通过ViewModel进行发送/接收数据传递
 *
 * @author attect
 * @date 2019-05-04
 */
open class WebSocketService : StaticViewModelLifecycleService() {
    lateinit var serviceViewModel: WebSocketServiceViewModel
        private set

    private var webSocket: WebSocket? = null

    private var webSocketListener = MyWebSocketListener()

    private var notificationId: Int = Int.MIN_VALUE

    private val handshakeHeaders: ArrayList<WebSocketHandshakeHeader> = arrayListOf()

    private var pingInterval:Long = 0L

    private val stringDataQueue = LinkedList<String>()

    private val bytesDataQueue = LinkedList<ByteString>()

    override fun onCreate() {
        super.onCreate()
        if (instanceCreateLock) return
        instanceCreateLock = true
        getViewModel(this)?.let {
            serviceViewModel = it
        }
        serviceViewModel.sendStringData.observe(this, Observer { content ->
            content?.let {
                webSocket?.send(it)
            }
        })
        serviceViewModel.sendBytesData.observe(this, Observer { content ->
            content?.let {
                webSocket?.send(it)
            }
        })
        serviceViewModel.stopByUser.observe(this, Observer {
            if (it) {
                Log.d(TAG, "stop signal from user")
                disconnectFromServer()
            }
        })
        serviceViewModel.receiveStringData.observe(this, Observer { content ->
            if (content == null && stringDataQueue.size > 0) {
                serviceViewModel.receiveStringData.value = stringDataQueue.poll()
            }
        })
        serviceViewModel.receiveBytesData.observe(this, Observer { content ->
            if (content == null && bytesDataQueue.size > 0) {
                serviceViewModel.receiveBytesData.value = bytesDataQueue.poll()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val defaultResult = super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId)
        //这个方法全局只允许被跑一次
        if (instanceCommandLock) return defaultResult
        instanceCommandLock = true

        if (intent == null) Log.e(TAG, "onStartCommand intent is null")
        intent?.let {
            //获得服务器地址
            if (intent.hasExtra(CONFIG_SERVER)) {
                if (intent.hasExtra(CONFIG_HANDSHAKE_HEADERS)) {
                    handshakeHeaders.clear()
                    handshakeHeaders.addAll(
                        intent.getParcelableArrayListExtra(
                            CONFIG_HANDSHAKE_HEADERS
                        )
                    )
                }
                if(intent.hasExtra(CONFIG_PING_INTERVAL)) pingInterval = intent.getLongExtra(
                    CONFIG_PING_INTERVAL,0L)
                serverUrl = intent.getStringExtra(CONFIG_SERVER)
                connectToServer()
            } else {
                return defaultResult //没有设定服务器地址
            }
            //如果需要创建前台服务
            if (intent.hasExtra(CONFIG_CREATE_NOTIFICATION_ID) && intent.hasExtra(
                    CONFIG_CREATE_NOTIFICATION
                )
            ) {
                notificationId = intent.getIntExtra(CONFIG_CREATE_NOTIFICATION_ID, Int.MIN_VALUE)
                notificationId.let {
                    if (it > Int.MIN_VALUE) {
                        startForeground(
                            it, intent.getParcelableExtra(
                                CONFIG_CREATE_NOTIFICATION
                            )
                        )
                    }
                }
            }
        }

        return defaultResult
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        instanceCreateLock = false
        instanceCommandLock = false

        //清除viewModel中的发送数据
        serviceViewModel.sendStringData.value = null
        serviceViewModel.sendBytesData.value = null
        serviceViewModel.status.value = null
    }

    /**
     * 连接到服务器
     * 同时重置用户手动停止状态
     */
    private fun connectToServer() {
        if (serviceViewModel.stopByUser.value == true) {
            serviceViewModel.stopByUser.postValue(false)
        }

        serverUrl?.let {
            serviceViewModel.status.postValue(WebSocketStatus.CONNECTING)
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.pingInterval(pingInterval,TimeUnit.MILLISECONDS)
            okHttpBuilder.retryOnConnectionFailure(true)
            okHttpBuilder.connectTimeout(5000,TimeUnit.MILLISECONDS)
            val requestBuilder = Request.Builder().url(it)
            handshakeHeaders.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
            webSocket = okHttpBuilder.build().newWebSocket(requestBuilder.build(), webSocketListener)
            return
        }
        throw IllegalStateException("Can't connect to WebSocket server because serverUrl is null")
    }

    private fun reconnectToServer(webSocket: WebSocket) {
        webSocket.cancel()
        //自动重连
        if (serviceViewModel.stopByUser.value != true) {
            serviceViewModel.status.postValue(WebSocketStatus.RECONNECTING)
            Log.w(TAG, "disconnect by environment, auto reconnect after 5000ms")
            Thread {
                try {
                    Thread.sleep(5000)
                    if (serviceViewModel.stopByUser.value != true) connectToServer()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun disconnectFromServer() {
        if (serviceViewModel.status.value != WebSocketStatus.CONNECTED) return
        webSocket?.close(RFC6455CloseState.CLOSE_STATE_NORMAL.value, "disconnect by user")
    }

    inner class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            serviceViewModel.status.postValue(WebSocketStatus.CONNECTED)
            Log.d(TAG, "WebSocket connected")
            if (serviceViewModel.stopByUser.value == true) disconnectFromServer() //针对从CONNECTING状态切换过来
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            serviceViewModel.status.postValue(WebSocketStatus.DISCONNECTED)
            Log.d(TAG, "WebSocket onFailure")
            t.printStackTrace()
            reconnectToServer(webSocket)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            serviceViewModel.status.postValue(WebSocketStatus.CLOSING)
            Log.e(TAG, "WebSocket is closing, because [$code]$reason ")
            if (serviceViewModel.stopByUser.value == true) {
                stopSelf()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d(TAG, "WebSocket onStringMessage:$text")
            stringDataQueue.offer(text)
            serviceViewModel.receiveStringData.postValue(null) //触发队列,必须直接post null
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            Log.d(TAG, "WebSocket onHexMessage:${bytes.size}")
            bytesDataQueue.offer(bytes)
            serviceViewModel.receiveBytesData.postValue(null) //触发队列,必须直接post null
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            serviceViewModel.status.postValue(WebSocketStatus.DISCONNECTED)
            Log.e(TAG, "WebSocket is closed, because [$code]$reason ")
            if (serviceViewModel.stopByUser.value == true) {
                webSocket.cancel()
                stopSelf()
            }
        }
    }

    companion object {
        private const val TAG = "WSS"

        /**
         * 在StaticViewModelStore中对应注册的key
         */
        private const val WEB_SOCKET_STATIC_VIEW_STORE_KEY = "[ws]"

        /**
         * 服务器地址的key
         */
        const val CONFIG_SERVER = "serverAddress"

        /**
         * 握手请求头参数
         */
        const val CONFIG_HANDSHAKE_HEADERS = "handshakeHeaders"

        /**
         * 伴随一个通知启动
         * 通知id的key
         */
        const val CONFIG_CREATE_NOTIFICATION_ID = "withNotification_ID"

        /**
         * 伴随一个通知启动
         * notification的key
         */
        const val CONFIG_CREATE_NOTIFICATION = "withNotification"

        /**
         * 心跳频率
         * 单位毫秒
         */
        const val CONFIG_PING_INTERVAL = "pingInterval"

        /**
         * onCreate方法锁
         * true时表示已经有一个逻辑实例在跑了
         */
        private var instanceCreateLock = false

        /**
         * onStartCommand方法锁
         * 创建Service的方法始终会导致startCommand方法的调用，因此需要一个针对内部逻辑的锁
         * true时表示已经有一个逻辑实例在跑了
         */
        private var instanceCommandLock = false

        /**
         * 服务器地址
         */
        private var serverUrl: String? = null


        /**
         * 启动服务
         * @param context App上下文
         * @param serverUrl 服务器地址
         * @param handshakeHeaders 握手HTTP自定义头部数据
         * @param pingInterval 心跳ping间隔，0禁用，单位毫秒
         */
        @JvmOverloads
        fun startService(
            context: Context,
            serverUrl: String,
            handshakeHeaders: ArrayList<WebSocketHandshakeHeader> = arrayListOf(),
            pingInterval :Long= 0L
        ) {
            context.startService(Intent(context, WebSocketService::class.java).apply {
                putExtra(CONFIG_SERVER, serverUrl)
                putParcelableArrayListExtra(CONFIG_HANDSHAKE_HEADERS, handshakeHeaders)
                putExtra(CONFIG_PING_INTERVAL,pingInterval)
            })
        }

        /**
         * 启动服务，同时绑定到一个前台通知
         * @param context App上下文
         * @param serverUrl 服务器地址
         * @param notificationId 已经创建的通知的id
         * @param notification 已经创建的通知本体
         * @param handshakeHeaders 握手HTTP自定义头部数据
         * @param pingInterval 心跳ping间隔，0禁用，单位毫秒
         */
        @JvmOverloads
        fun startService(
            context: Context,
            serverUrl: String,
            notificationId: Int,
            notification: Notification,
            handshakeHeaders: ArrayList<WebSocketHandshakeHeader> = arrayListOf(),
            pingInterval :Long= 0L
        ) {
            context.startService(Intent(context, WebSocketService::class.java).apply {
                putExtra(CONFIG_SERVER, serverUrl)
                putExtra(CONFIG_CREATE_NOTIFICATION_ID, notificationId)
                putExtra(CONFIG_CREATE_NOTIFICATION, notification)
                putParcelableArrayListExtra(CONFIG_HANDSHAKE_HEADERS, handshakeHeaders)
                putExtra(CONFIG_PING_INTERVAL,pingInterval)
            })
        }

        @JvmStatic
        fun getViewModel(caller: StaticViewModelStore.StaticViewModelStoreCaller): WebSocketServiceViewModel? {
            return caller.getStaticViewModel(
                WEB_SOCKET_STATIC_VIEW_STORE_KEY,
                WebSocketServiceViewModel::class.java
            )
        }

    }
}