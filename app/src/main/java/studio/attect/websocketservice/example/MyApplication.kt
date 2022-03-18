package studio.attect.websocketservice.example

import android.app.Application
import studio.attect.staticviewmodelstore.StaticViewModelStore

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StaticViewModelStore.application = this
    }
}