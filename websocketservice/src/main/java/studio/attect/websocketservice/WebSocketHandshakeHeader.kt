package studio.attect.websocketservice

import android.os.Parcel
import android.os.Parcelable

data class WebSocketHandshakeHeader(val key: String, val value: String):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WebSocketHandshakeHeader> {
        override fun createFromParcel(parcel: Parcel): WebSocketHandshakeHeader {
            return WebSocketHandshakeHeader(parcel)
        }

        override fun newArray(size: Int): Array<WebSocketHandshakeHeader?> {
            return arrayOfNulls(size)
        }
    }
}