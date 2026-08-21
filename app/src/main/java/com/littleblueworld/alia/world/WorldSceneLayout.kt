package com.littleblueworld.alia.world

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.littleblueworld.alia.R
import kotlin.math.roundToInt

class WorldSceneLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthDp = measuredWidth / resources.displayMetrics.density
        val responsiveScale = WorldLayoutSpec.scaleForWidth(widthDp)

        managedChildren().forEach { (child, spec) ->
            val childWidth = dp(spec.widthDp * responsiveScale)
            val childHeight = dp(spec.heightDp * responsiveScale)
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY),
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        managedChildren().forEach { (child, spec) ->
            val rect = WorldLayoutCalculator.place(
                viewportWidth = width,
                viewportHeight = height,
                objectWidth = child.measuredWidth,
                objectHeight = child.measuredHeight,
                spec = spec,
            )
            child.layout(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    private fun managedChildren(): List<Pair<View, WorldObjectSpec>> = buildList {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val objectId = child.worldObjectId() ?: continue
            add(child to WorldLayoutSpec.objects.getValue(objectId))
        }
    }

    private fun View.worldObjectId(): WorldObjectId? = when (id) {
        R.id.cloud_far -> WorldObjectId.CLOUD_FAR
        R.id.cloud_near -> WorldObjectId.CLOUD_NEAR
        R.id.camera_landmark -> WorldObjectId.CAMERA
        R.id.wish_landmark -> WorldObjectId.WISH
        R.id.star_landmark -> WorldObjectId.STAR
        R.id.bottle_landmark -> WorldObjectId.BOTTLE
        R.id.author_note_landmark -> WorldObjectId.AUTHOR_NOTE
        R.id.shell_landmark -> WorldObjectId.SHELL
        R.id.cafe_landmark -> WorldObjectId.CAFE
        else -> null
    }

    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).roundToInt()
}
