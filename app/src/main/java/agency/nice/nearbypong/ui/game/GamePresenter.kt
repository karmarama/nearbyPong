package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.model.Game
import agency.nice.nearbypong.model.Player
import agency.nice.nearbypong.repositories.GameRepository
import agency.nice.nearbypong.repositories.PlayerRepository
import agency.nice.nearbypong.ui.core.BasePresenter
import agency.nice.nearbypong.utils.Utils
import android.util.Log

/**
 * Created by fernando.moyano on 08/09/2017.
 */

const val TAG = "GamePresenter"

class GamePresenter(var gameRepository: GameRepository, var playerRepository: PlayerRepository) :
    BasePresenter<GameMvp.View>(), GameMvp.Presenter {

    fun saveOrUpdatePlayer(player: Player) {
        disposables.add(playerRepository.save(player)
            .compose(Utils.applySchedulersCompletable())
            .subscribe({
                view!!.showSaved()
                Log.d(TAG, "saved Player")
            })
            { throwable ->
                Log.e(TAG, "Error saving Player ", throwable)
                view!!.showNotSaved()
            })
    }

    fun saveGame(game: Game, winner: Boolean) {
        disposables.add(gameRepository.save(game)
            .compose(Utils.applySchedulersCompletable())
            .subscribe({
                view!!.navigateToResultScreen(winner)
                Log.d(TAG, "Game saved")
            })
            { throwable ->
                Log.e(TAG, "Error saving Game", throwable)
                view!!.navigateToResultScreen(winner)
            })
    }
}
