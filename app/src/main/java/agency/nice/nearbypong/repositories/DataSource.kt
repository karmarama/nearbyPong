package agency.nice.nearbypong.repositories

import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by ferranribell on 16/10/2017.
 */
interface DataSource<T>{
    fun save(item: T): Completable
    fun getAll(): Flowable<List<T>>
}