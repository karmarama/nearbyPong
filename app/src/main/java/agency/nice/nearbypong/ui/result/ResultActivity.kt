package agency.nice.nearbypong.ui.result

import agency.nice.nearbypong.R
import agency.nice.nearbypong.databinding.ActivityResultBinding
import agency.nice.nearbypong.helpers.PLAY_GAME
import agency.nice.nearbypong.helpers.trackEvent
import agency.nice.nearbypong.model.GAME_TAG
import agency.nice.nearbypong.model.Game
import agency.nice.nearbypong.ui.core.Constants.Companion.SIDE_RIGHT
import agency.nice.nearbypong.ui.game.GameActivity
import agency.nice.nearbypong.ui.game.ResultMvp
import agency.nice.nearbypong.ui.home.HomeActivity
import agency.nice.nearbypong.utils.getUserId
import agency.nice.nearbypong.utils.screenHeight
import agency.nice.nearbypong.widgets.Button
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.android.synthetic.main.activity_result.*


const val RESULT_ACTIVITY_TAG = "ResultActivity"
const val SIDE_TAG = "side"

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

fun Context.getResultIntent(isWinner: Boolean, game: Game, side: Int): Intent {
    return Intent(this, ResultActivity::class.java).apply {
        putExtra(RESULT_ACTIVITY_TAG, isWinner)
        putExtra(SIDE_TAG, side)
        putExtra(GAME_TAG, game)
    }
}

class ResultActivity : AppCompatActivity(), ResultMvp.View {
    private val SHOW: Boolean = true
    private val SECOND_MILLIS: Long = 1000
    lateinit var binding: ActivityResultBinding
    lateinit var presenter: ResultPresenter
    private var isWinner: Boolean = false
    private var game: Game = Game()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result)
        presenter = ResultPresenter()
        presenter.attachView(this)
        loadAnimationWithDelay(playAgain, !SHOW)
        loadAnimationWithDelay(quit, !SHOW)
        if (intent!!.hasExtra(RESULT_ACTIVITY_TAG)) {
            isWinner = intent.extras.getBoolean(RESULT_ACTIVITY_TAG)
            constraint.setBackgroundResource(if (intent.extras.getInt(SIDE_TAG) == SIDE_RIGHT) R.color.orange else R.color.cyan)
            game = intent.extras.getSerializable(GAME_TAG) as Game
            result.text = getString(R.string.result_score, game.scoreOne.toString(), game.scoreTwo.toString())

        }
        initButtons()
        initTitle()
    }

    private fun initButtons() {
        playAgain.setColorBlack()
        playAgain.setOnClickListener({
            trackEvent(this, PLAY_GAME, getUserId())
            startActivity(GameActivity.getIntent(this))
            finish()
        })

        quit.setColorBlack()
        quit.setOnClickListener({
            startActivity(HomeActivity.getIntent(this))
            finish()
        })
    }

    private fun initTitle() {
        description.text = getString(if (isWinner) R.string.result_description_text_winner else R.string.result_description_text_loser)
        val viewPropertyAnimator = description.animate().alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
        viewPropertyAnimator.duration = SECOND_MILLIS
        viewPropertyAnimator.start()
    }


    private fun loadAnimationWithDelay(button: Button, show: Boolean) {
        button.visibility = View.VISIBLE
        var viewPropertyAnimator: ViewPropertyAnimator = button.animate().translationY(if (show) 0f else screenHeight.toFloat()).setInterpolator(AccelerateDecelerateInterpolator())
        viewPropertyAnimator.withLayer().duration = SECOND_MILLIS
        viewPropertyAnimator.withLayer().startDelay = SECOND_MILLIS
        viewPropertyAnimator.start()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        loadAnimationWithDelay(playAgain, SHOW)
        loadAnimationWithDelay(quit, SHOW)
    }


}
