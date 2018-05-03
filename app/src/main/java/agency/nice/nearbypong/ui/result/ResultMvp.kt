package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.ui.core.BaseMvp

/**
 * Created by fernando.moyano on 08/09/2017.
 */
interface ResultMvp : BaseMvp {
    interface View : BaseMvp.BaseView
    interface Presenter : BaseMvp.BasePresenter<View>
}