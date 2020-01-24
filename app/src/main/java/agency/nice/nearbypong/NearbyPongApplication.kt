package agency.nice.nearbypong

import agency.nice.nearbypong.local.NearbyPongDatabase
import android.app.Application
import androidx.room.Room

/**
 * Created by fernando.moyano on 21/09/2017.
 */
class NearbyPongApplication : Application() {

    companion object {
        lateinit var database: NearbyPongDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, NearbyPongDatabase::class.java, "NearbyPongDatabase").build()
    }
}
