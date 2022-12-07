package com.example.flabbygame.ui.activity

import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.flabbygame.R
import com.example.flabbygame.customview.GameSurfaceView
import com.example.flabbygame.databinding.ActivityMainBinding
import com.example.flabbygame.game_obj.*
import com.example.flabbygame.ui.activity.StartActivity.Companion.KEY_COLOR_SET
import com.example.flabbygame.ui.activity.StartViewModel.Companion.MUSIC_KEY
import com.example.flabbygame.ui.activity.StartViewModel.Companion.SOUND_KEY
import com.example.flabbygame.ui.game_over.GameOverFragment
import com.example.flabbygame.ui.pause_screen.PauseFragment
import com.example.flabbygame.util.GameValue
import com.example.flabbygame.util.Constant.BEST_SCORE
import com.example.flabbygame.util.LogUtils
import com.example.flabbygame.util.SharedPrefs
import kotlinx.coroutines.*

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var bird = Bird()
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0

    private var dangerColumn: Column? = null

    private var frameFallingCounter = 0

    private var uiJob: Job? = null
    private var calculatorJob: Job? = null

    private val spaceHeight: Float = Column.Util.spaceHeight
    private val columnDistance: Float = Column.Util.distanceBetweenColumns

    private val listCol = ArrayList<Column>()

    private var isRunning = false
    private var isAlive = true
    private var isPausing = false
    private var colorSet: ColorSet? = null

    private lateinit var gameOverFragment: GameOverFragment
    private lateinit var pauseFragment: PauseFragment

    private lateinit var soundPool: SoundPool
    private var soundTrack = SoundTrack()

    private var mediaTheme: MediaPlayer? = null
    private var userPoint: Int = 0

    private var isSoundOff = true
    private var isMusicOff = true

    private val pieces = ArrayList<BallPiece>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        initData()
        initSoundPool()
        initView()
        initGameView()
        setUpStartState()
        initFragment()
    }

    private fun initData() {
        colorSet = intent.getParcelableExtra(KEY_COLOR_SET)
        isMusicOff = intent.getBooleanExtra(MUSIC_KEY, true)
        isSoundOff = intent.getBooleanExtra(SOUND_KEY, true)
        LogUtils.d("ColorSet: ${colorSet?.bird}")
        for (i in 0..7) {
            pieces.add(BallPiece())
        }
    }

    private fun initGameView() {
        with(binding.gameView) {
            holder.addCallback(this@MainActivity)
            setTapAction(GameSurfaceView.OnTapEvent {
                if (!isRunning && isAlive && !isPausing) {
                    isRunning = true
                }
                if (!isPausing) {
                    frameFallingCounter = 0
                }

                playTouchSound()
            })
            if (colorSet != null) {
                setColor(colorSet!!)
            } else {
                setColor(
                    bird = getColor(R.color.bird),
                    column = getColor(R.color.column),
                    background = getColor(R.color.background)
                )
            }
        }
    }

    private fun initView() {
        binding.txtCountDown.visibility = View.INVISIBLE
    }

    private fun initFragment() {
        gameOverFragment = GameOverFragment()
        gameOverFragment.setClickEvent(GameOverFragment.EventClick {
            supportFragmentManager.beginTransaction().remove(gameOverFragment).commit()
            newGame()
        })

        pauseFragment = PauseFragment()
        pauseFragment.setClickEvent(object : PauseFragment.PauseScreenEvent {
            override fun onHomeClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                this@MainActivity.onBackPressed()
            }

            override fun onNewGameClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                restartGame()
            }

            override fun onContinueClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                resume()
            }
        })
    }

    private fun setUpStartState() {
        setUpStartScore()
        setBirdStartState()
        initListColumn()
    }

    private fun setUpStartScore() {
        userPoint = 0
        binding.gameView.setScore(userPoint)
    }

    private fun initListColumn() {
        listCol.clear()
        for (i in 0 until Column.Util.columnsSize) {
            listCol.add(
                Column(
                    spaceX = screenWidth + i * columnDistance,
                    spaceY = randomSpaceY()
                )
            )
        }
        binding.gameView.setListColumn(listCol)
    }

    private fun randomSpaceY() = Column.Util.randomSpaceY(
        screenHeight = screenHeight,
        spaceHeight = spaceHeight,
        minHeight = Column.Util.columnMinHeight
    )

    private fun setBirdStartState() {
        bird.cy = screenHeight / 2f
        bird.cx = screenWidth * 10f / 100f

        with(binding.gameView) {
            setBirdIsAlive(true)
            setBirdY(bird.cy)
            setBirdX(bird.cx)
        }
    }

    private fun initJob() {
        calculatorJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val startCalculatorLoopTime = SystemClock.elapsedRealtime()
                /**
                 * Event bird touch to limit of screen
                 */
                if (bird.cy + bird.radius > screenHeight || bird.cy - bird.radius < 0) {
                    setDieEvent()
                }

                /**
                 * Calculator new bird's position after each delta time (@deltaT)
                 */
                if (isRunning) {
                    with(GameValue) {
                        val t = frameFallingCounter * deltaT
                        bird.cy += deltaT * acceleration * (t - 0.5f * deltaT) - bird.jumpPower * deltaT
                        binding.gameView.setBirdY(bird.cy)
                    }
                    frameFallingCounter++
                }

                /**
                 * Check event bird touch to column
                 */
                val col = listCol[0]
                if (col.spaceX - col.width / 2f <= bird.cx + bird.radius
                    && col.spaceX + col.width / 2f >= bird.cx - bird.radius
                ) {
                    if (col.spaceY - col.spaceHeight / 2f >= bird.cy - bird.radius
                        || col.spaceY + col.spaceHeight / 2f <= bird.cy + bird.radius
                    ) {
                        setDieEvent()
                    }
                }

                /**
                 * Check event plus point
                 */
                if (col !== dangerColumn) {
                    if (col.spaceX + col.width / 2f < bird.cx - bird.radius) {
                        binding.gameView.setScore(++userPoint)
                        playGetPointSound()
                        dangerColumn = col
                    }
                }

                /**
                 * Add new Column come and remove Column was out of screen
                 */
                if (isRunning && listCol.isNotEmpty()) {
                    if (listCol[0].spaceX + listCol[0].width / 2f <= 0) {
                        listCol.add(
                            Column(
                                spaceX = listCol[listCol.size - 1].spaceX + columnDistance,
                                spaceY = randomSpaceY()
                            )
                        )
                        listCol.removeAt(0)
                    }

                    listCol.forEach { c ->
                        c.spaceX -= c.moveSpeed * GameValue.deltaT
                    }
                    binding.gameView.setListColumn(listCol)
                }

                /**
                 * Delay per loop
                 */
                val endCalculatorLoopTime = SystemClock.elapsedRealtime()
                val sleepTime =
                    GameValue.deltaTInCal - (endCalculatorLoopTime - startCalculatorLoopTime)
                delay(sleepTime)
            }
        }

        uiJob = lifecycleScope.launch(newSingleThreadContext("RenderThread")) {
            var canvas: Canvas?
            val dt = 1000f / 60f
            var curTime = SystemClock.elapsedRealtime()
            while (isActive) {
                canvas = null
                val newTime = SystemClock.elapsedRealtime()
                val frameTime = (newTime - curTime)
                curTime = newTime

                with(binding.gameView) {
                    try {
                        synchronized(holder) {
                            canvas = holder.lockCanvas()
                            render(canvas)
                        }
                    } finally {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            }
        }
    }

    private fun setDieEvent() {
        isRunning = false
        isAlive = false

        playDieSound()

        pieces.forEach {
            it.x = bird.cx
            it.y = bird.cy
            it.radius = Bird.Util.viewRadius
            it.randomNewAngle()
        }

        binding.gameView.setPieces(pieces)
        binding.gameView.setBirdIsAlive(false)
        gameOverFragment.arguments = bundleOf(KEY_SCORE to userPoint)
        supportFragmentManager.beginTransaction()
            .replace(binding.overlayView.id, gameOverFragment, null)
            .commit()

        calculatorJob?.cancel()

        calculatorJob = null


        if ((SharedPrefs[BEST_SCORE, Int::class.java] ?: 0) < userPoint) {
            SharedPrefs.put(BEST_SCORE, userPoint)
        }
    }

    private fun restartGame() {
        isRunning = false
        isAlive = false
        calculatorJob?.cancel()

        calculatorJob = null

        if ((SharedPrefs[BEST_SCORE, Int::class.java] ?: 0) < userPoint) {
            SharedPrefs.put(BEST_SCORE, userPoint)
        }
        newGame()
    }

    private fun newGame() {
        setUpStartState()
        isRunning = false
        isPausing = false
        isAlive = true
        initJob()
    }

    private fun pause() {
        isRunning = false
        isPausing = true
        supportFragmentManager.beginTransaction()
            .add(binding.overlayView.id, pauseFragment, null)
            .commit()
        LogUtils.d("pause")
    }

    private fun resume() {
        lifecycleScope.launch {
            binding.txtCountDown.visibility = View.VISIBLE
            for (i in 3 downTo 1) {
                binding.txtCountDown.text = i.toString()
                delay(1000L)
            }
            isRunning = true
            isPausing = false

            binding.txtCountDown.visibility = View.INVISIBLE
            LogUtils.d("resume")
        }
    }

    private fun initSoundPool() {
        val attrsSound = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool
            .Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrsSound)
            .build()

        soundTrack.touch = soundPool.load(this, R.raw.touch_sound, 1)
        soundTrack.die = soundPool.load(this, R.raw.sound_die, 1)
        soundTrack.point = soundPool.load(this, R.raw.sound_get_point, 1)

        mediaTheme = MediaPlayer.create(this, R.raw.them_sound)
        mediaTheme?.isLooping = true
    }

    /**
     * => soundID id của file âm thanh return từ method load()
     * => leftVolume volume (range = 0.0 to 1.0)
     * => rightVolume volume (range = 0.0 to 1.0)
     * => priority (0 = lowest priority)
     * => loop (0 = no loop, -1 = loop forever)
     * => rate speedup (1.0 = normal playback, range 0.5 to 2.0)
     */

    private fun playThemeSound() {
        if (!isMusicOff) {
            mediaTheme?.start()
        }
    }

    private fun playTouchSound() {
        if (!isSoundOff) {
            soundTrack.touch?.let {
                soundPool.play(
                    it,
                    1f,
                    1f,
                    1,
                    0,
                    1f,
                )
            }
        }
    }

    private fun playDieSound() {
        if (!isSoundOff) {
            soundTrack.die?.let {
                soundPool.play(
                    it,
                    0.8f,
                    0.8f,
                    1,
                    0,
                    1f,
                )
            }
        }
    }

    private fun playGetPointSound() {
        if (!isSoundOff) {
            soundTrack.point?.let {
                soundPool.play(
                    it,
                    0.6f,
                    0.6f,
                    1,
                    0,
                    1f,
                )
            }
        }
    }

    override fun onPause() {
        if (isRunning) {
            pause()
        }
        LogUtils.d("Stop")
        super.onPause()
    }

    override fun onDestroy() {
        binding.gameView.visibility = View.GONE
        soundPool.release()
        LogUtils.d("Release and destroy game play")
        mediaTheme?.release()
        mediaTheme = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isRunning) {
            pause()
        } else {
            super.onBackPressed()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        LogUtils.d("Create surface")
        initJob()
        playThemeSound()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        calculatorJob?.cancel()
        uiJob?.cancel()

        calculatorJob = null
        uiJob = null
        mediaTheme?.stop()
    }

    companion object {
        const val KEY_SCORE = "key_score"
    }
}