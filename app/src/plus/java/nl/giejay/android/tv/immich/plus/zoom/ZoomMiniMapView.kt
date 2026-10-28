package nl.giejay.android.tv.immich.plus.zoom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * Overview of a zoomed photo: the photo's outline, scaled into this view, and the currently
 * visible part as a highlighted frame. [visible] is in fractions (0..1) of the photo.
 */
class ZoomMiniMapView(context: Context) : View(context) {
    private var aspectRatio = 1f
    private val visible = RectF(0f, 0f, 1f, 1f)
    private val density = resources.displayMetrics.density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xB3000000.toInt() }
    private val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = 0x99FFFFFF.toInt()
    }
    private val visibleFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x40FFFFFF }
    private val visibleStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = 0xFFFFFFFF.toInt()
    }

    fun update(photoAspectRatio: Float, visiblePart: RectF) {
        aspectRatio = if (photoAspectRatio.isFinite() && photoAspectRatio > 0f) photoAspectRatio else 1f
        visible.set(
            visiblePart.left.coerceIn(0f, 1f),
            visiblePart.top.coerceIn(0f, 1f),
            visiblePart.right.coerceIn(0f, 1f),
            visiblePart.bottom.coerceIn(0f, 1f)
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val pad = 8 * density
        val maxW = width - 2 * pad
        val maxH = height - 2 * pad
        // fit the photo's outline into the box, keeping its proportions
        val (w, h) = if (maxW / maxH > aspectRatio) maxH * aspectRatio to maxH else maxW to maxW / aspectRatio
        val left = (width - w) / 2
        val top = (height - h) / 2
        val photo = RectF(left, top, left + w, top + h)

        canvas.drawRoundRect(RectF(photo.left - pad, photo.top - pad, photo.right + pad, photo.bottom + pad), 8 * density, 8 * density, backgroundPaint)
        canvas.drawRect(photo, photoPaint)
        val part = RectF(
            photo.left + visible.left * w,
            photo.top + visible.top * h,
            photo.left + visible.right * w,
            photo.top + visible.bottom * h
        )
        canvas.drawRect(part, visibleFill)
        canvas.drawRect(part, visibleStroke)
    }
}
