package com.littleblueworld.alia.wish

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.littleblueworld.alia.R
import com.littleblueworld.alia.state.WishRules
import kotlin.math.floor

class PixelHoldButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextView(context, attrs) {
    var canStartHold: () -> Boolean = { true }
    var onRejected: () -> Unit = UnitCallback
    var onCompleted: () -> Unit = UnitCallback

    private val density = resources.displayMetrics.density
    private val inactivePaint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.lavender)
        alpha = 90
    }
    private val activePaint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.soft_pink)
    }
    private var progressAnimator: ValueAnimator? = null
    private var progress = 0f
    private var completed = false

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!canStartHold()) {
                    onRejected()
                    return true
                }
                parent?.requestDisallowInterceptTouchEvent(true)
                startHold()
                return true
            }

            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (!completed) resetProgressAnimated()
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (!completed) resetProgressAnimated()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val gap = 3f * density
        val horizontalPadding = 12f * density
        val available = width - horizontalPadding * 2f -
            gap * (WishRules.HOLD_PROGRESS_BLOCKS - 1)
        val blockWidth = available / WishRules.HOLD_PROGRESS_BLOCKS
        val blockHeight = 5f * density
        val top = height - 10f * density
        val activeBlocks = floor(progress * WishRules.HOLD_PROGRESS_BLOCKS + 0.0001f).toInt()
        for (index in 0 until WishRules.HOLD_PROGRESS_BLOCKS) {
            val left = horizontalPadding + index * (blockWidth + gap)
            canvas.drawRect(
                left,
                top,
                left + blockWidth,
                top + blockHeight,
                if (index < activeBlocks) activePaint else inactivePaint,
            )
        }
    }

    fun stop() {
        progressAnimator?.removeAllListeners()
        progressAnimator?.cancel()
        progressAnimator = null
        progress = 0f
        completed = false
        invalidate()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun startHold() {
        progressAnimator?.cancel()
        completed = false
        progressAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = WishRules.HOLD_DURATION_MS
            addUpdateListener { animator ->
                progress = animator.animatedValue as Float
                invalidate()
                if (progress >= 1f && !completed) {
                    completed = true
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onCompleted()
                }
            }
            start()
        }
    }

    private fun resetProgressAnimated() {
        progressAnimator?.cancel()
        progressAnimator = ValueAnimator.ofFloat(progress, 0f).apply {
            duration = RESET_DURATION_MS
            addUpdateListener { animator ->
                progress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private companion object {
        const val RESET_DURATION_MS = 220L
        val UnitCallback: () -> Unit = {}
    }
}
