package studio.attect.websocketservice

import androidx.lifecycle.Observer
import okio.ByteString

/**
 * 二进制数据接收观察器
 * 会自动触发WebSocketServer让其提交下一个数据
 *
 * @author attect
 * @date 2019-05-08
 */
abstract class BytesDataObserver(private val webSocketServiceViewModel: WebSocketServiceViewModel) :
    Observer<ByteString?> {

    final override fun onChanged(b: ByteString?) {
        webSocketServiceViewModel.requireNextBytesData()
        b?.let {
            onReceive(it)
        }
    }

    abstract fun onReceive(b: ByteString)
}