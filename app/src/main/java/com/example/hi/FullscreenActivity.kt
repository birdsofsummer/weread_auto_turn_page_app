package com.example.hi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import android.view.View
import android.widget.SeekBar
//import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_fullscreen.*

import kotlinx.coroutines.*
import io.reactivex.rxkotlin.*
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

//import org.jetbrains.anko.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

import java.io.File
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit
import java.text.*

import android.util.DisplayMetrics


















// shell("ls /")
fun shell(cmd:String){
    val pid=Runtime.getRuntime().exec(cmd)
   // echo(pid)
}

fun shell1(cmd:String){
    Runtime.getRuntime().exec(arrayOf("/system/bin/su", "-c", cmd))
}
fun shells(cmds: List<String>){
    cmds.map{
        Runtime.getRuntime().exec(arrayOf("/system/bin/su", "-c", it))
    }
}


fun remount_cmd():List<String> {
    val t =arrayOf("/","/system").map {
        "mount -o remount,rw $it"
    }
    return t

}

fun now(): String{
    val t=SimpleDateFormat("yyyyMMddHHmmsSSS").format(Date())
    return t
}


//截图
fun shot() {
    val path="/tmp/"
    val t=now()
    val shot="screencap -p $path$t.png"
    val shot1=remount_cmd() + shot
    shells(shot1)
    kill()
}

fun kill(key:String="tencent"){
    val cmd="pkill -9 $key"
    shell1(cmd)
}

fun start_weread(){
    val cmd="am start -n com.tencent.weread/com.tencent.weread.ReaderFragmentActivity"
    shell1(cmd)
}
fun stop_read(){
    kill("tencent")
    kill("sh")
}
fun turn_page(n:Int=4){
     val t="sh /sdcard/kanshu $n"
     shell1(t)
     start_weread()
}



fun get_position(s:Array<Int>):Array<Int>{
    val w=s[0]
    val h=s[1]
    val s1=arrayOf(w-1,h-20)
    return s1
}

fun tap_cmd(s:Array<Int>):String{
    val x=s[0]
    val y=s[1]
    val t="input tap  $x $y"
    return t
}



class FullscreenActivity : AppCompatActivity() {
    var reading=true
    var speed=5
    var tap_osition="input tap 1079 500"

    fun change_speed(n:Int){
        speed=n
        val t="${n}秒/页"
        show_sleep_time.setText(t)
        echo(t)
    }
    fun seek_speed(){
        val t=seekBar.progress
        change_speed(t)
    }
    private fun toggle() {
        val t=seekBar.progress
        speed=t
        show_sleep_time.setText("$t")
        echo("${t}秒/页")
    }

    fun device_info():Array<Int>{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var width = displayMetrics.widthPixels
        var height = displayMetrics.heightPixels
       // echo("$width x $height")
        return arrayOf(width,height)
    }
    fun get_tap_cmd():String{
          val s=get_position(device_info())
          return tap_cmd(s)
    }
    private fun tap_r(){
          shell1(tap_osition)
    }
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        show_sleep_time.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }


    val kill_weread =View.OnTouchListener {_,_->
        stop_read()
        echo("bye")
        false
    }

    fun begin_read(){
        echo("请打开书架上要看的书 ...")
        val t=seekBar.progress
        turn_page(t)
    }

    fun auto_read(){
        start_weread()
        Thread.sleep(5000)
        while (reading){
            tap_r()
            val s=speed.toLong()*1000
            Thread.sleep(s)
        }
    }

    //-----------------------------------------------------4种sleep方法---------------------------------------------

    fun start_auto_read(){
        reading=true
        echo("请在5秒钟内打开书架上要看的书 ...")
        doAsync {
            auto_read()
        }
    }

    fun start_auto_read0(){
        echo("请在5秒钟内打开书架上要看的书 ...")
        reading=true
        GlobalScope.async {
            auto_read()
        }
    }
    fun start_auto_read1(){
        echo("请在5秒钟内打开书架上要看的书 ...")
        reading=true
        Thread {
            auto_read()
        }.start()
    }

    fun start_auto_read2(){
        start_weread()
        echo("请在5秒钟内打开书架上要看的书 ...")
        reading=true
        Thread {
            Thread.sleep(5000)
            Observable
                .interval(speed.toLong(), TimeUnit.SECONDS)
                //.take(2)
                .subscribe({
                    tap_r()
                });
        }.start()
    }
//-----------------------------------------------------4种sleep方法---------------------------------------------


    fun echo(i:Any){
        Toast.makeText(this,"${i}",Toast.LENGTH_LONG).show()
    }

    fun test_echo1(){

        val list = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        list.toObservable() // extension function for Iterables
            .filter { it.length >= 5 }
            .subscribeBy(  // named arguments for lambda Subscribers
                onNext = { toast(it) },
                onError =  { it.printStackTrace() },
                onComplete = { toast("Done!") }
            )
    }

    override fun onResume(){
        super.onResume()
        stop_read()
        reading=false
    }

    override fun onPause(){
        super.onPause()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummy_button1.setOnTouchListener(mDelayHideTouchListener)
//        dummy_button2.setOnTouchListener( View.OnTouchListener { _, _ ->
//            echo("dddd")
//            false
//        })
        show_sleep_time.setOnClickListener { toggle() }
       test_read.setOnClickListener {
           tap_osition=get_tap_cmd()
           start_auto_read()

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override  fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                            fromUser: Boolean) {
                //   progressView!!.text = progress.toString()
                //   seekbarStatusView!!.text = "Tracking Touch"
               // echo(progress)
                toggle()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // seekbarStatusView!!.text = "Started Tracking Touch"
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // seekbarStatusView!!.text = "Stopped Tracking Touch"
            }
        })
        //seekBar.setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener{echo("123")})
        //turn_page()

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        show_sleep_time.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
