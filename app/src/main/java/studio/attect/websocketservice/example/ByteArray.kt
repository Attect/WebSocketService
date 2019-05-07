package studio.attect.websocketservice.example

/**
 * 将byte数组转为可读字符串
 */
fun ByteArray.toHexString() = joinToString("") { "%02x ".format(it).toUpperCase() }