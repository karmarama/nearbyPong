package agency.nice.nearbypong.repositories

import agency.nice.nearbypong.local.dao.GameDao
import agency.nice.nearbypong.model.Game
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by fernando.moyano on 03/11/2017.
 */
class GameRepositoryTest {

    lateinit var gameDao: GameDao
    lateinit var game: Game

    var list = ArrayList<Game>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        gameDao = Mockito.mock(GameDao::class.java)
        game = Game(0L, "1L", "2L", 1, 0)
    }

    @Test
    fun getAll_emptyCase() {
        Mockito.`when`(gameDao!!.getAll()).thenReturn(Flowable.empty())
        var result = gameDao!!.getAll()
        var testSubscriber = TestSubscriber<List<Game>>()
        result.subscribe(testSubscriber)
        testSubscriber.assertComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(0)
        val listResult = testSubscriber.values()
        assertThat(listResult.size, `is`(0))
    }

    @Test
    fun getAll_nonEmptyCase() {
        list.add(game)

        Mockito.`when`(gameDao!!.getAll()).thenReturn(Flowable.just(list))
        var result = gameDao!!.getAll()


        var testSubscriber = TestSubscriber<List<Game>>()
        result.subscribe(testSubscriber)
        testSubscriber.assertComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(1)
        val listResult = testSubscriber.values()[0]
        assertThat(listResult.size, `is`(1))
        assertThat(listResult[0].playerOneId, `is`("1L"))
    }

}