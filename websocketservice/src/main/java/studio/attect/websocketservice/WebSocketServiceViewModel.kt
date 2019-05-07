package studio.attect.websocketservice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okio.ByteString

/**
 * WebSocketService与其它接口进行数据交换的ViewModel
 *
 * @author attect
 * @date 2019-05-04
 */
class WebSocketServiceViewModel :ViewModel(){
    /**
     * 接收到的字符串数据
     */
    public val receiveStringData = MutableLiveData<String?>()

    /**
     * 接收到的二进制数据
     */
    public val receiveBytesData = MutableLiveData<ByteString?>()

    /**
     * 要发送的字符串数据
     */
    public val sendStringData = MutableLiveData<String?>()

    /**
     * 要发送的二进制数据
     */
    public val sendBytesData = MutableLiveData<ByteString?>()


    /**
     * 给服务断开连接的信号
     * 注意不要将其内容设为null
     */
    public val stopByUser = MutableLiveData<Boolean>()

    /**
     * 服务状态
     * 模糊的
     */
    public val status = MutableLiveData<WebSocketStatus>()


}