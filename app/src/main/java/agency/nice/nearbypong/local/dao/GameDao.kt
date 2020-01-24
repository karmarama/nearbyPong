package agency.nice.nearbypong.local.dao

import agency.nice.nearbypong.model.Game
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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