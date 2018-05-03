package agency.nice.nearbypong.local.dao

import agency.nice.nearbypong.model.Game
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

/**
 * Created by fernando.moyano on 20/09/2017.
 */
@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(game: Game)

    @Query("select * from game")
    fun getAll(): Flowable<List<Game>>
}