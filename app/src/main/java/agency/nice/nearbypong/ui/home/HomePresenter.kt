package agency.nice.nearbypong.ui.home

import agency.nice.nearbypong.repositories.GameRepository
import agency.nice.nearbypong.ui.core.BasePresenter
import agency.nice.nearbypong.ui.game.HomeMvp
import agency.nice.nearbypong.utils.Utils
import android.util.Log

/**
 * Created by fernando.moyano on 08/09/2017.
 */
class HomePresenter(private val gameRepository: GameRepository) : BasePresenter<HomeMvp.View>(),
    HomeMvp.Presenter {

    private val TAG = "HomePresenter"
    override fun loadGames() {
        disposables.add(
            gameRepository.getAll()
                .compose(Utils.applySchedulersFlowable())
                .subscribe({ gamesList ->
                    if (gamesList.isEmpty()) {
                        view!!.hideGamesList()
                    } else {
                        view!!.loadGames(gamesList)
                    }
                }, { throwable ->
                    Log.e(TAG, "Error retrieving the games", throwable)
                    view!!.hideGamesList()
                })
        )
    }
}
