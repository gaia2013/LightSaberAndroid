package com.example.hitoshi.lightsaverapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var mgr: SensorManager
    private lateinit var sensor: Sensor // どのセンサーを使いたいか指定するために用いる

    private lateinit var soundPool: SoundPool
    private var audioOne: Int by Delegates.notNull() // 起動音
    private var audioTwo: Int by Delegates.notNull() // 振った時の音
    private var audioThree: Int by Delegates.notNull() // 振った時の音2
    private var audioFour: Int by Delegates.notNull() // 振った時の音3
    private var audioFive: Int by Delegates.notNull() //  振った時の音4

    private lateinit var toggleButton: ToggleButton
    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton

    private var startAccel: Boolean = false // 降っている最中か否か

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //viewが設定される

        mgr = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 加速度センサーを取得する
        sensor = mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        // あくまでもキャストを試みる。対応していなければクラッシュします。
        // 絶対にキャストが成功するということが保証されている場合のみ使用する。

        // レイアウトからボタンを取得する
        toggleButton = findViewById(R.id.toggleButton)
        radioButton1 = findViewById(R.id.radioButton1)
        radioButton2 = findViewById(R.id.radioButton2)
        radioButton3 = findViewById(R.id.radioButton3)
        radioButton4 = findViewById(R.id.radioButton4)

        var attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

        soundPool = SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(1)
                .build()
                .apply {
                    audioOne = load(this@MainActivity, R.raw.light_saber1, 1)
                    audioTwo = load( this@MainActivity, R.raw.light_saber3,1)
                    audioThree = load(this@MainActivity, R.raw.blanketing, 1)
                    audioFour = load(this@MainActivity, R.raw.sf_police,1)
                    audioFive = load(this@MainActivity, R.raw.unanalyzable, 1)
                }

        toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                soundPool.play(
                        audioOne,
                        1f,
                        1f,
                        1,
                        0,
                        1f
                )
            }
        }
    }

    // センサーの値が変化したことを検知するために、対応するリスナーを作ります。
    private val listener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            // 受け取ったイベントが使いたいセンサーからではないか？ || ToggleButtonがOFFか？
            if (event.sensor != sensor || !toggleButton.isChecked) return
            // return はメソッドの返り値を設定する場所。何も書いていないから何も返さない。

            val x = Math.abs(event.values[0])
            val y = Math.abs(event.values[1])
            val z = Math.abs(event.values[2])

            val synthetic = x + y + z

            if (!startAccel && synthetic > 8) {
                startAccel = true

                val checkedRadioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)

                soundPool.play(
                    when(checkedRadioButton){
                        radioButton1 -> audioTwo
                        radioButton2 -> audioThree
                        radioButton3 -> audioFour
                        radioButton4 -> audioFive
                        else -> audioTwo
                    },
                    1f,
                    1f,
                    1,
                    0,
                    1f
                )

            } else if (startAccel && synthetic < 1) {
                startAccel = false
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }

    override fun onResume() {
        super.onResume()
        mgr.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mgr.unregisterListener(listener)
    }
}
