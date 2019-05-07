package studio.attect.websocketservice

/**
 * WebSocketService的状态
 * 模糊的
 * 包含实际连接的状态，和一些逻辑状态
 *
 * 注意它们的切换可能是跳跃的
 *
 * @author attect
 * @date 2019-05-04
 */
enum class WebSocketStatus {

    /**
     * 已断开连接
     * 刚启动或网络错误以及重连尝试间的状态
     */
    DISCONNECTED,

    /**
     * 正在连接
     */
    CONNECTING,

    /**
     * 正在等待重新连接
     */
    RECONNECTING,

    /**
     * 已经连接，通信正常
     */
    CONNECTED,

    /**
     * 正在关闭连接
     */
    CLOSING
}