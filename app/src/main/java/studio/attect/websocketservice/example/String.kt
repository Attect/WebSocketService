package studio.attect.websocketservice.example

private const val HEX_CHARS = "0123456789ABCDEF"

/**
 * 将可读十六进制字符串转换为byte数组
 * 空格将会被消除
 */
fun String.hexStringToByteArray() : ByteArray {
    val str = this.replace(" ","")
    if(str.length % 2 > 0){
        return (str+"F").hexStringToByteArray()
    }

    val result = ByteArray(str.length / 2)

    for (i in 0 until str.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(str[i].toUpperCase());
        val secondIndex = HEX_CHARS.indexOf(str[i + 1].toUpperCase());

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }

    return result
}