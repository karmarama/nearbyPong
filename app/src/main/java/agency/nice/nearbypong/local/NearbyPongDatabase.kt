package agency.nice.nearbypong.local

import agency.nice.nearbypong.local.dao.GameDao
import agency.nice.nearbypong.local.dao.PlayerDao
import agency.nice.nearbypong.model.Game
import agency.nice.nearbypong.model.Player
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by fernando.moyano on 20/09/2017.
 */

@Database(version = 1, entities = [Player::class, Game::class], exportSchema = false)
abstract class NearbyPongDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
}