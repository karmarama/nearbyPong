package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.model.Game
import agency.nice.nearbypong.ui.core.BaseMvp

/**
 * Created by fernando.moyano on 08/09/2017.
 */
interface HomeMvp : BaseMvp {
    interface View : BaseMvp.BaseView {
        fun loadGames(games: List<Game>)
        fun hideGamesList()
    }
    interface Presenter : BaseMvp.BasePresenter<View>{
        fun loadGames()
    }
}