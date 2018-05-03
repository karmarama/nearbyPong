package agency.nice.nearbypong.repositories

import agency.nice.nearbypong.local.dao.PlayerDao
import agency.nice.nearbypong.model.Player
import io.reactivex.Completable
import io.reactivex.Completable.fromAction
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by fernando.moyano on 20/09/2017.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class PlayerRepository(private val playerDao: PlayerDao): DataSource<Player> {

    override fun save(player: Player): Completable {
        return fromAction{
            playerDao.savePlayer(player)
        }
    }

    override fun getAll(): Flowable<List<Player>> {
        return playerDao.getAllPlayers()
    }

    fun getPlayer(id: String): Single<Player> {
        return playerDao.getPlayer(id)
    }
}