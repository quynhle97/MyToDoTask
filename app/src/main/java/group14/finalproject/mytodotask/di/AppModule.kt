package group14.finalproject.mytodotask.di

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(application: Application) {

    private val mApplication: Application = application

    @Singleton
    @Provides
    fun application() : Application {
        return mApplication
    }
}