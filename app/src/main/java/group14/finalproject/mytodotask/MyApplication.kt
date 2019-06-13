package group14.finalproject.mytodotask

import android.app.Application
import group14.finalproject.mytodotask.di.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper

class MyApplication: Application() {
    private lateinit var mAppComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesHelper.init(applicationContext)

        mAppComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .taskModule(TaskModule(this))
            .build()
    }

    fun getAppComponent(): AppComponent {
        return mAppComponent
    }
}