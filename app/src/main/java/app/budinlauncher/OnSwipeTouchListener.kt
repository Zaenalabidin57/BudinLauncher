package app.budinlauncher

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import kotlin.math.abs
import java.util.Timer
import java.util.TimerTask

open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private var longPressOn = false
    private val gestureDetector: GestureDetector
    private val vibrator: Vibrator

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun performHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            longPressOn = false
        }
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(motionEvent: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
            return super.onSingleTapUp(motionEvent)
        }

        override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(motionEvent)
        }

        override fun onLongPress(motionEvent: MotionEvent) {
            longPressOn = true
            onLongClick()
            super.onLongPress(motionEvent)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) {
                            performHapticFeedback()
                            onSwipeUp()
                        } else {
                            onSwipeDown()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }

    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onLongClick() {}
    open fun onDoubleClick() {}
    open fun onTripleClick() {}
    open fun onClick() {}
}