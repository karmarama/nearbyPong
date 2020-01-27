package agency.nice.nearbypong.ui.home

import agency.nice.nearbypong.NearbyPongApplication
import agency.nice.nearbypong.R
import agency.nice.nearbypong.helpers.PLAY_GAME
import agency.nice.nearbypong.helpers.trackEvent
import agency.nice.nearbypong.model.Game
import agency.nice.nearbypong.repositories.GameRepository
import agency.nice.nearbypong.ui.game.GameActivity
import agency.nice.nearbypong.ui.game.HomeMvp
import agency.nice.nearbypong.utils.getUserId
import agency.nice.nearbypong.utils.screenHeight
import agency.nice.nearbypong.utils.screenWidth
import agency.nice.nearbypong.widgets.Ball
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

private const val TIMEOUT_BOUNCING: Long = 3500
private const val SECOND_MILLIS: Long = 1000
private const val SHOW: Boolean = true

class HomeActivity : AppCompatActivity(), HomeMvp.View {
    lateinit var presenter: HomePresenter

    companion object {
        fun getIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        showListGames(!SHOW)
        showTitle(SHOW)
        showButtons(SHOW)
        showBall(SHOW)
        startLineAnimation(!SHOW)
        ball.rotate()
        ball.crazyBounce()
        startAnimationTimer()
    }

    override fun loadGames(games: List<Game>) {
        listGames.setHasFixedSize(true)
        var layoutManager = LinearLayoutManager(this)
        listGames.layoutManager = layoutManager

        listGames.adapter = GamesAdapter(this, games)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        presenter = HomePresenter(GameRepository(NearbyPongApplication.database.gameDao()))
        presenter.attachView(this)
        presenter.loadGames()
        play.setOnClickListener {
            trackEvent(this, PLAY_GAME, getUserId())
            startActivity(GameActivity.getIntent(this))
        }
        scores.setOnClickListener {
            showListGames(SHOW)
            showTitle(!SHOW)
            showButtons(!SHOW)
            showBall(!SHOW)
        }
        initBall()
    }

    private fun showButtons(show: Boolean) {
        play.visibility = if (show) View.VISIBLE else View.GONE
        scores.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showTitle(show: Boolean) {
        title_first.visibility = if (show) View.VISIBLE else View.GONE
        title_second.visibility = if (show) View.VISIBLE else View.GONE
        title_gap.visibility = if (show) View.INVISIBLE else View.GONE
        title_fourth.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showListGames(show: Boolean) {
        title_scores.visibility = if (show) View.VISIBLE else View.GONE
        listGames.visibility = if (show) View.VISIBLE else View.GONE
        startLineAnimation(show)

    }

    private fun showBall(show: Boolean) {
        ball.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initBall() {
        ball.bottomLimit = screenHeight
        ball.sideLimit = screenWidth
        ball.listener = object : Ball.BallListener {
            override fun goal() {

            }

            override fun sendBallData(
                posX: Float,
                posY: Float,
                velocityX: Float,
                velocityY: Float
            ) {
            }
        }
    }

    override fun hideGamesList() {
        showListGames(!SHOW)
    }

    private fun startLineAnimation(show: Boolean) {
        if (show) {
            line.visibility = View.VISIBLE

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    ContextCompat.getColor(this, R.color.colorAccent),
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    ContextCompat.getColor(this, R.color.transparent)
                )
            )
            line.background = gradientDrawable

            val flingAnimationY = FlingAnimation(line, DynamicAnimation.Y).setFriction(0.1f)

            flingAnimationY.addUpdateListener { _, value, _ ->
                if (value <= (screenHeight / 3)) {
                    flingAnimationY.friction = 0.9f
                }
            }

            line.y = screenHeight.toFloat()
            line.x = (screenWidth / 2).toFloat()
            flingAnimationY.setStartVelocity(-1000f).start()
        } else {
            line.visibility = View.GONE
        }

    }


    private fun startAnimationTimer() {

        object : CountDownTimer(TIMEOUT_BOUNCING, SECOND_MILLIS) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("TIMER", "seconds remaining: " + millisUntilFinished / SECOND_MILLIS)
            }

            override fun onFinish() {
                ball.cancelFlings()
                ball.animate()
                    .x(title_gap.x)
                    .y(title_gap.y)
                    .setDuration(SECOND_MILLIS)
                    .withEndAction {
                        ball.changeAlphaToMax()
                    }
                    .start()
            }
        }.start()

    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (line.visibility == View.VISIBLE) {
            onResume()
        } else {
            super.onBackPressed()
        }
    }
}
