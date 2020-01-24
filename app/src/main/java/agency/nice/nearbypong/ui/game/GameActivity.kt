package agency.nice.nearbypong.ui.game

import agency.nice.nearbypong.NearbyPongApplication
import agency.nice.nearbypong.R
import agency.nice.nearbypong.helpers.FRIEND_FOUND
import agency.nice.nearbypong.helpers.GAME_OVER
import agency.nice.nearbypong.helpers.RETRY_CONNECTION
import agency.nice.nearbypong.helpers.trackEvent
import agency.nice.nearbypong.model.*
import agency.nice.nearbypong.repositories.GameRepository
import agency.nice.nearbypong.repositories.PlayerRepository
import agency.nice.nearbypong.ui.core.Constants
import agency.nice.nearbypong.ui.result.getResultIntent
import agency.nice.nearbypong.utils.getUserId
import agency.nice.nearbypong.utils.screenHeight
import agency.nice.nearbypong.utils.screenWidth
import agency.nice.nearbypong.widgets.Ball
import agency.nice.nearbypong.widgets.Bar
import agency.nice.nearbypong.widgets.Endpoint
import agency.nice.nearbypong.widgets.RadarView.OnAnimationFinished
import agency.nice.nearbypong.widgets.ResizeAnimation
import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.connection.Payload
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_game.*
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

private const val TIMEOUT_RETRY_MILLIS: Long = 5000
private const val DARK_SCORE: Int = 4
private const val FINISH_ANIMATION: Boolean = true
private const val SHOW: Boolean = true
private const val WINNER: Boolean = true

class GameActivity : ConnectionsActivity(), GameMvp.View, OnAnimationFinished {
    private var isBlinking = false

    private var hasHitBar: Boolean = false
    lateinit var presenter: GamePresenter
    lateinit var palId: String
    private var timer: Timer = Timer()
    private val players: ArrayList<Player> = ArrayList()
    private lateinit var currentPlayer: Player
    private lateinit var collisionTimer: CollisionTimerTask
    private lateinit var bounceSound: MediaPlayer
    private lateinit var player: Player
    private lateinit var initialBallConstraint: ConstraintSet

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, GameActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        setContentView(R.layout.activity_game)
        initCircles()
        presenter = GamePresenter(
            GameRepository(NearbyPongApplication.database.gameDao()),
            PlayerRepository(NearbyPongApplication.database.playerDao())
        )
        presenter.attachView(this)

        currentPlayer = Player("", Constants.SIDE_LEFT, 0)
        initialBallConstraint = ConstraintSet()
        initialBallConstraint.clone(constraint)
        bounceSound = MediaPlayer.create(this, R.raw.beep_ping)

        showConnecting(SHOW)
        showCircles(SHOW)

        retry.setOnClickListener {
            trackEvent(this, RETRY_CONNECTION, getUserId())
            resetState()
            onStateChanged(State.SEARCHING)
            showRetryAnimation(!SHOW)
            showTimeoutAnimation(!SHOW)
            showConnecting(SHOW)
            showCircles(SHOW)
            connectionFailed.visibility = View.GONE
        }
        initCollisionTimer()
    }

    private fun initCollisionTimer() {
        collisionTimer = CollisionTimerTask(object : CollisionTimerTask.OnCollision {
            override fun hit() {
                if (intersects(ball, bar) && !hasHitBar) {
                    hasHitBar = true
                    runOnUiThread {
                        ball.fillInsideColor()
                        bar.blink()
                        ball.barBounce()
                        ball.changeDrawable(currentPlayer.side)
                        bounceSound.start()

                        collisionTimer.cancel()
                    }
                }
            }
        })
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onDestroy() {
        presenter.detachView()
        presenter.onDestroy()
        collisionTimer.cancel()
        timer.cancel()
        super.onDestroy()
    }

    private fun setPlayers(idOponent: String) {
        var mySide =
            if (getUserId().compareTo(idOponent) > 0) Constants.SIDE_LEFT else Constants.SIDE_RIGHT
        currentPlayer = Player(getDeviceId(), mySide, 0)
        var oponentPlayer = Player(idOponent, 1 - mySide, 0)

        players.add(currentPlayer)
        players.add(oponentPlayer)


        presenter.saveOrUpdatePlayer(currentPlayer)
        presenter.saveOrUpdatePlayer(oponentPlayer)

        player = currentPlayer
    }

    private fun startTimer() {
        initCollisionTimer()
        timer = Timer()
        timer.scheduleAtFixedRate(collisionTimer, 500, 1)
    }

    private fun initCircles() {
        TransitionManager.beginDelayedTransition(constraint)
        circles.setDeviceDimensions(screenWidth, screenHeight)
        circles.setAnimationFinishedCallback(this)
    }

    private fun initBar() {
        bar.setImageResource(if (currentPlayer.side == Constants.SIDE_RIGHT) R.drawable.rectangle_orange else R.drawable.rectangle_green)
        bar.side = currentPlayer.side
        if (bar.side == Constants.SIDE_RIGHT) {

            val barConstraint = ConstraintSet()
            barConstraint.clone(constraint)
            barConstraint.clear(bar.id, ConstraintSet.LEFT)
            barConstraint.connect(
                bar.id,
                ConstraintSet.RIGHT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                0
            )
            barConstraint.applyTo(constraint)

            val scoreConstraint = ConstraintSet()
            scoreConstraint.apply {
                clone(constraint)
                clear(score.id, ConstraintSet.RIGHT)
                connect(
                    score.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT,
                    0
                )
                applyTo(constraint)

            }
        }

        bar.bottomLimit = screenHeight
        bar.visibility = View.VISIBLE
        bar.height = resources.getDimensionPixelOffset(R.dimen.bar_height)


        Log.d(TAG, "SIDE " + bar.side.toString())
    }


    fun intersects(ball: Ball, rect: Bar): Boolean {
        var intersects = false
        val radio = ball.width / 2

        if (
            ((currentPlayer.side == Constants.SIDE_LEFT && ball.x - radio <= rect.getTheRight())
                    || (currentPlayer.side == Constants.SIDE_RIGHT && ball.x + radio >= rect.getTheLeft()))
            && ball.y - radio >= rect.getTheTop() - radio
            && ball.y + radio <= rect.getTheBottom() + radio
        ) {
            intersects = true
        }

        return intersects
    }


    private fun initBall() {
        ball.apply {
            rotate()
            side = currentPlayer.side
            bottomLimit = screenHeight
            sideLimit = screenWidth
            listener = object : Ball.BallListener {
                override fun goal() {
                    Log.d("BALL", Constants.GOAL)
                    if (!getConnectedEndpoints().isEmpty()) {
                        hasHitBar = false
                        ball.stop()
                        sendMsg(
                            NearbyMessage(
                                goal = Goal(
                                    Constants.GOAL,
                                    currentPlayer.id,
                                    currentPlayer.score
                                )
                            )
                        )
                        setFriendGoal(players[1].score + 1)
                        presenter.saveOrUpdatePlayer(
                            Player(
                                id = players[1].id,
                                side = players[1].side,
                                score = players[1].score
                            )
                        )
                        checkDarkScreen(players[1].score)
                        bar.changeBarSize(currentPlayer.score - players[1].score)
                        TransitionManager.beginDelayedTransition(constraint)
                        resetBall()
                    }
                }

                override fun sendBallData(
                    posX: Float,
                    posY: Float,
                    velocityX: Float,
                    velocityY: Float
                ) {
                    Log.d("BALL", "Send view_ball data:$posX-$posY")
                    ball.stop()
                    hasHitBar = false
                    if (!getConnectedEndpoints().isEmpty()) {
                        sendMsg(
                            NearbyMessage(
                                position = BallParameters(
                                    posX,
                                    posY,
                                    velocityX,
                                    velocityY,
                                    Constants.SENT
                                )
                            )
                        )
                    }
                }
            }
        }

    }

    override fun showSaved() {
        Log.d("GAME", "new Data saved")
    }

    override fun showNotSaved() {
        Log.d("GAME", "data not saved")
    }

    private fun resetBall() {
        if (currentPlayer.side == Constants.SIDE_LEFT) {
            ball.x = bar.x + bar.width / 2 + ball.width / 2
        } else {
            ball.x = bar.x - bar.width / 2 - ball.width / 2
        }
        ball.visibility = View.VISIBLE
    }

    override fun onReceive(endpoint: Endpoint?, payload: Payload) {
        if (payload.type == Payload.Type.BYTES) {
            val result = String(payload.asBytes()!!, Charset.forName("UTF-8"))
            appendResultToLogs(result)

            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(NearbyMessage::class.java)
            val state = jsonAdapter.fromJson(result)

            when {
                state!!.id.isNotEmpty() -> {
                    palId = state.id
                    setPlayers(palId)
                    createOpponent(palId)
                    sendMsg(NearbyMessage(confirmation = Constants.CONFIRMATION_VALUE))
                }
                state.confirmation.isNotEmpty() -> {
                    trackEvent(this, FRIEND_FOUND, getUserId())
                    showTimeoutAnimation(!SHOW)
                    showRetryAnimation(!SHOW)
                    showConnecting(!SHOW)
                    showCircles(!SHOW)
                    initScore()
                    initBar()
                    initBall()
                    startTimer()
                    ball.barBounce()
                }
                state.position.sent.isNotEmpty() -> {
                    ball.changeDrawable(1 - currentPlayer.side)
                    ball.moveBallTo(state.position)
                    startTimer()
                }
                state.goal.player.isNotEmpty() -> {
                    goalAnimation(state.goal.score)
                }
                state.end.isNotEmpty() -> {
                    trackEvent(this, GAME_OVER, getUserId())
                    if (state.end == getString(R.string.game_loser_flag)) {
                        saveGame(!WINNER)
                    }
                }
            }
        }
    }

    private fun saveGame(winner: Boolean) {
        var game = Game()
        game.apply {
            playerOneId = getUserId()
            playerTwoId = players[1].id
            scoreOne = currentPlayer.score
            scoreTwo = players[1].score
        }

        presenter.saveGame(game, winner)
    }

    override fun addPlayers(opponent: Player) {
        var mySide =
            if (getUserId().compareTo(opponent.id) > 0) Constants.SIDE_LEFT else Constants.SIDE_RIGHT
        currentPlayer = Player(getDeviceId(), mySide, 0)
        players.add(currentPlayer)
        players.add(opponent)
        presenter.saveOrUpdatePlayer(opponent)

        player = currentPlayer
    }

    override fun createOpponent(opponentId: String) {
        var mySide =
            if (getUserId().compareTo(opponentId) > 0) Constants.SIDE_LEFT else Constants.SIDE_RIGHT
        currentPlayer = Player(getDeviceId(), mySide, 0)
        var opponentPlayer = Player(opponentId, 1 - mySide, 0)

        addPlayers(opponentPlayer)
    }

    private fun initScore() {
        score.visibility = View.VISIBLE
        var color = R.color.cyan
        if (currentPlayer.side == Constants.SIDE_RIGHT) {
            color = R.color.orange
        }
        score.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun initScoreWeakReference(scoreWR: WeakReference<TextView>) {
        score.visibility = View.VISIBLE
        var color = R.color.cyan
        if (currentPlayer.side == Constants.SIDE_RIGHT) {
            color = R.color.orange
        }
        scoreWR.get()!!.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun setFriendGoal(friendScore: Int) {
        players[1].score = friendScore
    }

    private fun goalAnimation(friendScore: Int) {
        score.animate()
            .setStartDelay(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(500)
            .translationX((if (currentPlayer.side == Constants.SIDE_LEFT) -1 else 1) * (screenWidth / 2 - score.width * 2).toFloat())
            .translationY((screenHeight / 2 - score.height * 2).toFloat())
            .scaleX(3f)
            .scaleY(3f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                    // Nothing
                }

                override fun onAnimationStart(p0: Animator?) {
                    // Nothing
                }

                override fun onAnimationCancel(p0: Animator?) {
                    // Nothing
                }

                override fun onAnimationEnd(p0: Animator?) {
                    currentPlayer.score++
                    score.text = currentPlayer.score.toString()
                    score.animate()
                        .translationX(0f)
                        .translationY(0f)
                        .setStartDelay(1000)
                        .setDuration(500)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(null)
                        .start()
                    checkGoals()
                    bar.changeBarSize(currentPlayer.score - friendScore)
                    TransitionManager.beginDelayedTransition(constraint)
                }

            })
            .start()
        setFriendGoal(friendScore)
    }

    private fun checkGoals() {
        if (currentPlayer.score >= Constants.MAX_GOALS) {
            sendMsg(NearbyMessage(end = getString(R.string.game_loser_flag)))
            saveGame(WINNER)
        }
    }

    private fun checkDarkScreen(scoreInt: Int) {
        if (scoreInt >= DARK_SCORE && !isBlinking) {
            val ballViewReference = WeakReference(ball)
            val barViewReference = WeakReference(bar)
            val scoreViewReference = WeakReference(score)
            val constraintViewReference = WeakReference(constraint)

            isBlinking = true
            startBlinkTimer(
                ballViewReference,
                barViewReference,
                scoreViewReference,
                constraintViewReference
            )
        }
    }

    private fun showRetry() {
        connecting.visibility = View.GONE
        retry.visibility = View.VISIBLE
    }

    private fun endGame() {
        ball.apply {
            stopRotate()
            visibility = View.GONE
        }
    }

    private fun fadeConnecting(show: Boolean) {
        val viewPropertyAnimator = connecting.animate().alpha(if (show) 1f else 0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
        viewPropertyAnimator.apply {
            duration = SECOND_MILLIS
            start()
        }

    }

    private fun showCircles(show: Boolean) {
        circles.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            circles.start(!FINISH_ANIMATION)
        }
    }

    private fun showConnecting(show: Boolean) {
        connecting.visibility = if (show) View.VISIBLE else View.GONE

        fadeConnecting(show)
        if (show) {
            connecting.animate().alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator()).start()
        } else {
            val ballConstraint = ConstraintSet()
            ballConstraint.apply {
                clone(constraint)
                clear(ball.id, ConstraintSet.TOP)
                clear(ball.id, ConstraintSet.BOTTOM)
                clear(ball.id, ConstraintSet.LEFT)
                clear(ball.id, ConstraintSet.RIGHT)
                connect(ball.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                connect(
                    ball.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    0
                )
            }

            if (currentPlayer.side == Constants.SIDE_LEFT) {
                ball.visibility = View.VISIBLE
                ballConstraint.apply {
                    connect(ball.id, ConstraintSet.LEFT, bar.id, ConstraintSet.RIGHT, 0)
                    applyTo(constraint)
                }

            } else {
                ball.x = (-1 * ball.width).toFloat()
            }

            TransitionManager.beginDelayedTransition(constraint)
        }

    }


    private fun showRetryAnimation(show: Boolean) {
        retry.animate().translationY(if (show) 0f else screenHeight.toFloat())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withLayer().duration = SECOND_MILLIS
    }

    private fun showTimeoutAnimation(show: Boolean) {
        circles.visibility = if (show) View.GONE else View.VISIBLE
        timeoutText.visibility = if (show) View.VISIBLE else View.GONE
        var resizeAnimation = ResizeAnimation().apply {
            //  initTimeoutView()
            background.visibility = View.VISIBLE
            init(background, screenHeight * 2, screenWidth, show)
            duration = TimeUnit.SECONDS.toMillis(2)
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    connectionFailed.visibility = if (show) View.VISIBLE else View.GONE
                    if (!show) background.visibility = View.GONE
                    timeoutText.visibility = View.GONE
                }
            })
        }

        background.apply {
            visibility = View.VISIBLE
            x = 0f
            y = 0f
            startAnimation(resizeAnimation)
        }
        if (show) {
            startRetryTimer()
        } else {
            showRetryAnimation(!SHOW)
        }
    }

    private fun startRetryTimer() {

        object : CountDownTimer(TIMEOUT_RETRY_MILLIS, SECOND_MILLIS) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("RETRY", "seconds remaining: " + millisUntilFinished / SECOND_MILLIS)
            }

            override fun onFinish() {
                showRetry()
                showRetryAnimation(SHOW)
                timeoutText.visibility = View.GONE
            }
        }.start()

    }

    private fun startBlinkTimer(
        ballWR: WeakReference<Ball>,
        barWR: WeakReference<Bar>,
        scoreWR: WeakReference<TextView>,
        constraintWR: WeakReference<ConstraintLayout>
    ) {


        object : Thread() {

            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(SECOND_MILLIS)
                        runOnUiThread {
                            if (Calendar.getInstance().get(Calendar.SECOND) % 2 == 0) {
                                setNormalScreen(ballWR, barWR, scoreWR, constraintWR)
                            } else {
                                setDarkScreen(ballWR, barWR, scoreWR, constraintWR)
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                }

            }
        }.start()

    }


    private fun setDarkScreen(
        ballWR: WeakReference<Ball>,
        barWR: WeakReference<Bar>,
        scoreWR: WeakReference<TextView>,
        constraintWR: WeakReference<ConstraintLayout>
    ) {
        constraintWR.get()!!.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (currentPlayer.side == Constants.SIDE_RIGHT) R.color.cyan else R.color.orange
            )
        )
        ballWR.get()!!.colourDarkBlue()
        barWR.get()!!.setDarkColour()
        scoreWR.get()!!.setTextColor(ContextCompat.getColor(this, R.color.blue))
    }

    private fun setNormalScreen(
        ballWR: WeakReference<Ball>,
        barWR: WeakReference<Bar>,
        scoreWR: WeakReference<TextView>,
        constraintWR: WeakReference<ConstraintLayout>
    ) {
        constraint.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.background_material_dark
            )
        )
        ballWR.get()!!.changeDrawable(currentPlayer.side)
        barWR.get()!!.setImageResource(if (currentPlayer.side == Constants.SIDE_RIGHT) R.drawable.rectangle_orange else R.drawable.rectangle_green)
        initScoreWeakReference(scoreWR)
    }


    private fun showSettingFieldText(isSetting: Boolean) {
        connecting.text =
            if (isSetting) getText(R.string.game_connecting_text) else getText(R.string.game_setting_side_text)
    }


    override fun onEndpointDisconnected(endpoint: Endpoint?) {
        Log.d(TAG, getString(R.string.log_disconnected, endpoint!!.name))
        setState(State.SEARCHING)
        endGame()
        showTimeoutAnimation(!SHOW)
        showConnecting(SHOW)
        showCircles(SHOW)
    }

    override fun onEndpointConnected(endpoint: Endpoint?) {
        Log.d(TAG, getString(R.string.log_connected, endpoint!!.name))
        setState(State.CONNECTED)
        showSettingFieldText(SHOW)

        sendMsg(NearbyMessage(getUserId()))
    }

    override fun onAnimationFinish() {
        connecting.visibility = View.GONE
        showTimeoutAnimation(SHOW)
    }

    override fun onTimeOutReached() {
        circles.start(FINISH_ANIMATION)
    }

    override fun navigateToResultScreen(winner: Boolean) {
        startActivity(
            getResultIntent(
                winner,
                Game(scoreOne = currentPlayer.score, scoreTwo = players[1].score),
                bar.side
            )
        )
    }

    internal class CollisionTimerTask(private val callback: OnCollision) : TimerTask() {

        override fun run() {
            callback.hit()
        }

        interface OnCollision {
            fun hit()
        }
    }
}