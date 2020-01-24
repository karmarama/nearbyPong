package agency.nice.nearbypong.repositories

import agency.nice.nearbypong.local.dao.GameDao
import agency.nice.nearbypong.model.Game
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by fernando.moyano on 20/09/2017.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class GameRepository(private val gameDao: GameDao) : DataSource<Game> {
    override fun getAll(): Flowable<List<Game>> {
        return gameDao.getAll()
    }

    override fun save(game: Game): Completable {
        return Completable.fromAction {
            gameDao.save(game)
        }
    }
}
