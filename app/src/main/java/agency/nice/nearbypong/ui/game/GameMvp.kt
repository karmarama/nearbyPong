package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.model.Player
import agency.nice.nearbypong.ui.core.BaseMvp

/**
 * Created by fernando.moyano on 08/09/2017.
 */
interface GameMvp : BaseMvp {
    interface View : BaseMvp.BaseView {
        fun showSaved()
        fun showNotSaved()
        fun addPlayers(opponent: Player)
        fun createOpponent(opponentId: String)
        fun navigateToResultScreen(winner: Boolean)
    }

    interface Presenter : BaseMvp.BasePresenter<View>
}