package studio.attect.websocketservice

import androidx.lifecycle.Observer

/**
 * 字符串数据接收观察器
 * 会自动触发WebSocketServer让其提交下一个数据
 *
 * @author attect
 * @date 2019-05-08
 */
abstract class StringDataObserver(private val webSocketServiceViewModel: WebSocketServiceViewModel) :
    Observer<String?> {

    final override fun onChanged(t: String?) {
        webSocketServiceViewModel.requireNextStringData()
        t?.let {
            onReceive(it)
        }
    }

    abstract fun onReceive(t: String)

}