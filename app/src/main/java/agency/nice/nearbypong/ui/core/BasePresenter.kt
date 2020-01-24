package agency.nice.nearbypong.ui.core

import io.reactivex.internal.disposables.ListCompositeDisposable

/**
 * Created by fernando.moyano on 08/09/2017.
 */
open class BasePresenter<V : BaseMvp.BaseView> : BaseMvp.BasePresenter<V> {

    protected var view: V? = null
    val disposables = ListCompositeDisposable()

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    fun isViewAttached(): Boolean {
        return view != null
    }

    fun onDestroy() {
        disposables.dispose()
    }

}