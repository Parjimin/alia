package com.littleblueworld.alia.gallery

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import kotlin.math.abs

class GalleryPagerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val previousPhoto = photoView()
    private val nextPhoto = photoView()
    private val currentPhoto = photoView()
    private val velocityThreshold = 600f * resources.displayMetrics.density
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var photoCount = 0
    private var currentIndex = 0
    private var labelForIndex: (Int) -> String = { "" }
    private var onIndexChanged: (Int) -> Unit = {}
    private var downX = 0f
    private var dragX = 0f
    private var velocityTracker: VelocityTracker? = null
    private var animating = false

    init {
        clipChildren = false
        clipToPadding = false
        isClickable = true
        addView(previousPhoto)
        addView(nextPhoto)
        addView(currentPhoto)
    }

    fun configure(
        count: Int,
        labelForIndex: (Int) -> String,
        onIndexChanged: (Int) -> Unit,
    ) {
        require(count > 0)
        photoCount = count
        this.labelForIndex = labelForIndex
        this.onIndexChanged = onIndexChanged
        currentIndex = 0
        bindWindow()
    }

    fun pauseMotion() {
        cancelAnimations()
        positionAtRest()
    }

    fun clear() {
        pauseMotion()
        velocityTracker?.recycle()
        velocityTracker = null
        previousPhoto.setImageDrawable(null)
        currentPhoto.setImageDrawable(null)
        nextPhoto.setImageDrawable(null)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        positionAtRest()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = photoCount > 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (photoCount == 0 || animating) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain().also { it.addMovement(event) }
                downX = event.x
                dragX = 0f
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                dragX = applyEdgeResistance(event.x - downX)
                positionForDrag(dragX)
                return true
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val velocity = velocityTracker?.xVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null
                if (abs(dragX) < touchSlop) {
                    performClick()
                    animateSnapBack()
                } else {
                    settle(velocity)
                }
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
                animateSnapBack()
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.isScrollable = photoCount > 1
        if (currentIndex < photoCount - 1) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)
        }
        if (currentIndex > 0) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
        }
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean = when (action) {
        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> {
            if (currentIndex >= photoCount - 1 || animating) false
            else {
                animateCommit(nextPhoto, -width.toFloat(), 1)
                true
            }
        }

        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> {
            if (currentIndex <= 0 || animating) false
            else {
                animateCommit(previousPhoto, width.toFloat(), -1)
                true
            }
        }

        else -> super.performAccessibilityAction(action, arguments)
    }

    private fun bindWindow() {
        if (photoCount == 0) return
        val window = GalleryWindow.around(currentIndex, photoCount)
        bind(previousPhoto, window.previous)
        bind(currentPhoto, window.current)
        bind(nextPhoto, window.next)
        contentDescription = labelForIndex(currentIndex)
        positionAtRest()
    }

    private fun bind(view: FocalCropImageView, index: Int?) {
        view.visibility = if (index == null) INVISIBLE else VISIBLE
        if (index == null) {
            view.setImageDrawable(null)
            return
        }
        view.setImageDrawable(GalleryPlaceholderDrawable(index + 1))
        view.setFocalOffset(0f, 0f)
        view.contentDescription = null
    }

    private fun settle(velocity: Float) {
        val result = GallerySwipeDecision.decide(
            index = currentIndex,
            count = photoCount,
            dragPx = dragX,
            widthPx = width.toFloat(),
            velocityPxPerSecond = velocity,
            velocityThresholdPxPerSecond = velocityThreshold,
        )
        when (result) {
            GallerySwipeResult.PREVIOUS -> animateCommit(previousPhoto, width.toFloat(), -1)
            GallerySwipeResult.NEXT -> animateCommit(nextPhoto, -width.toFloat(), 1)
            GallerySwipeResult.SNAP_BACK -> animateSnapBack()
        }
    }

    private fun animateCommit(incoming: FocalCropImageView, outgoingX: Float, indexDelta: Int) {
        animating = true
        currentPhoto.animate()
            .translationX(outgoingX)
            .rotation(if (outgoingX > 0f) MAX_ROTATION_DEGREES else -MAX_ROTATION_DEGREES)
            .alpha(0.78f)
            .setDuration(COMMIT_DURATION_MS)
            .withEndAction {
                currentIndex += indexDelta
                animating = false
                bindWindow()
                onIndexChanged(currentIndex)
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED)
            }
            .start()
        incoming.animate()
            .translationX(0f)
            .setDuration(COMMIT_DURATION_MS)
            .start()
    }

    private fun animateSnapBack() {
        animating = true
        currentPhoto.animate()
            .translationX(0f)
            .rotation(0f)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(SNAP_DURATION_MS)
            .withEndAction {
                animating = false
                positionAtRest()
            }
            .start()
        previousPhoto.animate().translationX(-width.toFloat()).setDuration(SNAP_DURATION_MS).start()
        nextPhoto.animate().translationX(width.toFloat()).setDuration(SNAP_DURATION_MS).start()
    }

    private fun positionForDrag(drag: Float) {
        val width = width.toFloat().coerceAtLeast(1f)
        val progress = (abs(drag) / width).coerceIn(0f, 1f)
        currentPhoto.apply {
            translationX = drag
            rotation = drag / width * MAX_ROTATION_DEGREES
            scaleX = 1f - SCALE_REDUCTION * progress
            scaleY = scaleX
        }
        previousPhoto.translationX = -width + drag.coerceAtLeast(0f)
        nextPhoto.translationX = width + drag.coerceAtMost(0f)
    }

    private fun positionAtRest() {
        if (width == 0) return
        cancelAnimations()
        previousPhoto.translationX = -width.toFloat()
        currentPhoto.apply {
            translationX = 0f
            rotation = 0f
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
        nextPhoto.translationX = width.toFloat()
        dragX = 0f
    }

    private fun applyEdgeResistance(rawDrag: Float): Float = when {
        currentIndex == 0 && rawDrag > 0f -> rawDrag * EDGE_RESISTANCE
        currentIndex == photoCount - 1 && rawDrag < 0f -> rawDrag * EDGE_RESISTANCE
        else -> rawDrag
    }

    private fun cancelAnimations() {
        previousPhoto.animate().cancel()
        currentPhoto.animate().cancel()
        nextPhoto.animate().cancel()
        animating = false
    }

    private fun photoView() = FocalCropImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private companion object {
        const val MAX_ROTATION_DEGREES = 2f
        const val SCALE_REDUCTION = 0.015f
        const val EDGE_RESISTANCE = 0.22f
        const val COMMIT_DURATION_MS = 220L
        const val SNAP_DURATION_MS = 180L
    }
}
