package agency.nice.nearbypong.local.dao

import agency.nice.nearbypong.model.Player
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by fernando.moyano on 20/09/2017.
 */
@Dao
interface PlayerDao {
    @Insert(onConflict = REPLACE)
    fun savePlayer(user: Player)

    @Query("select * from player")
    fun getAllPlayers(): Flowable<List<Player>>

    @Query("select * from player " +
            "where id = :userId")
    fun getPlayer(userId: String): Single<Player>
}