package agency.nice.nearbypong.ui.core

/**
 * Created by fernando.moyano on 08/09/2017.
 */
interface BaseMvp {

    /**
     * The interface Base view.
     */
    interface BaseView

    /**
     * The interface Base presenter.
     *
     * @param <V> the type parameter
    </V> */
    interface BasePresenter<in V : BaseView> {
        /**
         * Attach view.
         *
         * @param view the view
         */
        fun attachView(view: V)

        /**
         * Detach view.
         */
        fun detachView()
    }
}