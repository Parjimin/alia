package com.littleblueworld.alia.world

enum class WorldObjectId {
    CLOUD_FAR,
    CLOUD_NEAR,
    CAMERA,
    WISH,
    STAR,
    BOTTLE,
    AUTHOR_NOTE,
    SHELL,
    ISLAND,
    CAFE,
}

data class WorldObjectSpec(
    val centerX: Float,
    val centerY: Float,
    val widthDp: Float,
    val heightDp: Float,
)

data class WorldObjectRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

object WorldLayoutSpec {
    val objects: Map<WorldObjectId, WorldObjectSpec> = mapOf(
        WorldObjectId.CLOUD_FAR to WorldObjectSpec(0.18f, 0.14f, 240f, 96f),
        WorldObjectId.CLOUD_NEAR to WorldObjectSpec(0.76f, 0.34f, 176f, 80f),
        WorldObjectId.CAMERA to WorldObjectSpec(0.18f, 0.27f, 64f, 64f),
        WorldObjectId.WISH to WorldObjectSpec(0.50f, 0.20f, 52f, 52f),
        WorldObjectId.STAR to WorldObjectSpec(0.82f, 0.28f, 60f, 60f),
        WorldObjectId.BOTTLE to WorldObjectSpec(0.18f, 0.49f, 56f, 72f),
        WorldObjectId.AUTHOR_NOTE to WorldObjectSpec(0.82f, 0.49f, 64f, 64f),
        WorldObjectId.SHELL to WorldObjectSpec(0.17f, 0.73f, 52f, 52f),
        // The center island is already painted into world_day; this is its composition anchor.
        WorldObjectId.ISLAND to WorldObjectSpec(0.50f, 0.76f, 0f, 0f),
        WorldObjectId.CAFE to WorldObjectSpec(0.82f, 0.72f, 104f, 92f),
    )

    fun scaleForWidth(widthDp: Float): Float = (widthDp / REFERENCE_WIDTH_DP)
        .coerceIn(MIN_SCALE, MAX_SCALE)

    private const val REFERENCE_WIDTH_DP = 360f
    private const val MIN_SCALE = 0.88f
    private const val MAX_SCALE = 1.16f
}

object WorldLayoutCalculator {
    fun place(
        viewportWidth: Int,
        viewportHeight: Int,
        objectWidth: Int,
        objectHeight: Int,
        spec: WorldObjectSpec,
    ): WorldObjectRect {
        val centerX = viewportWidth * spec.centerX
        val centerY = viewportHeight * spec.centerY
        val left = (centerX - objectWidth / 2f).toInt()
        val top = (centerY - objectHeight / 2f).toInt()
        return WorldObjectRect(
            left = left,
            top = top,
            right = left + objectWidth,
            bottom = top + objectHeight,
        )
    }
}
