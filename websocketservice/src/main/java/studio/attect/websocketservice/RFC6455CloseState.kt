package studio.attect.websocketservice

/**
 * rfc6455连接关闭状态码
 * https://tools.ietf.org/html/rfc6455#section-7.4
 *
 * 部分状态码也可在这看到,名称与值相同
 * @see okhttp3.internal.ws.WebSocketProtocol
 *
 * @author attect
 * @date 2019-05-06
 */
enum class RFC6455CloseState constructor(i: Int) {
    /**
     * 默认值
     */
    NO_ERROR(0),

    /**
     * 主动关闭状态码：正常关闭
     */
    CLOSE_STATE_NORMAL(1000),

    /**
     * 主动关闭状态码：正在离开
     */
    CLOSE_STATE_GOING_AWAY(1001),

    /**
     * 主动关闭状态码：协议错误
     */
    CLOSE_STATE_PROTOCOL_ERROR(1002),

    /**
     * 主动关闭状态码：不可接受的数据类型
     */
    CLOSE_STATE_DATA_TYPE_ERROR(1003),

    /**
     * 主动关闭状态码：无法处理的消息类型
     */
    CLOSE_STATE_DATA_TYPE_CANT_HANDLE(1007),

    /**
     * 主动关闭状态码：服务器违反约定规则
     */
    CLOSE_STATE_SERVER_VIOLATE_RULE(1008),

    /**
     * 主动关闭状态码：收到的消息过大
     */
    CLOSE_STATE_MESSAGE_TO_LARGE(1009),

    /**
     * 主动关闭状态码：没有从服务器得到期望的内容
     */
    CLOSE_STATE_SERVER_NOT_RESPONSE_EXPECT_RESULT(1010),

    /**
     * 主动关闭状态码：服务器正在关闭
     */
    CLOSE_STATE_SERVER_IS_CLOSING(1011),

    /**
     * 主动关闭状态码：无法完成TLS握手
     * 可能是证书错误
     */
    CLOSE_STATE_SAFE_HANDSHAKE(1015);

    var value: Int = 0
        internal set

    init {
        value = i
    }
}
