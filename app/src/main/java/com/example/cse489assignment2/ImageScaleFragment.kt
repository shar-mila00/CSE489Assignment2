package com.example.cse489assignment2

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.databinding.FragmentImageScaleBinding

/**
 * Drawer option B - Image Scale.
 *
 * Shows the cat image (res/drawable-nodpi/cat.jpg) centered on the screen.
 * Pinch with two fingers to zoom in / out; drag with one finger to move the
 * image while zoomed in. Implemented with a [ScaleGestureDetector] that
 * updates the ImageView [Matrix].
 */
class ImageScaleFragment : Fragment() {

    companion object {
        private const val MAX_ZOOM = 6f // relative to the "fit to screen" size
    }

    private var _binding: FragmentImageScaleBinding? = null
    private val binding get() = _binding!!

    private val matrix = Matrix()
    private var baseScale = 1f      // scale that fits the image on screen
    private var currentScale = 1f   // current absolute scale
    private lateinit var scaleDetector: ScaleGestureDetector

    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageScaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Center + fit the image as soon as the view has its real size.
        binding.imageView.addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or2, ob ->
            if (r - l != or2 - ol || b - t != ob - ot) fitImageToCenter()
        }
        binding.imageView.post { fitImageToCenter() }

        scaleDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val minScale = baseScale * 0.5f
                    val maxScale = baseScale * MAX_ZOOM
                    var factor = detector.scaleFactor
                    val target = (currentScale * factor).coerceIn(minScale, maxScale)
                    factor = target / currentScale
                    currentScale = target
                    matrix.postScale(factor, factor, detector.focusX, detector.focusY)
                    applyMatrix()
                    return true
                }
            }
        )

        binding.imageView.setOnTouchListener { v, event ->
            scaleDetector.onTouchEvent(event)

            if (!scaleDetector.isInProgress) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.x; lastY = event.y; dragging = true
                    }
                    MotionEvent.ACTION_MOVE -> if (dragging && event.pointerCount == 1) {
                        matrix.postTranslate(event.x - lastX, event.y - lastY)
                        lastX = event.x; lastY = event.y
                        applyMatrix()
                    } else if (event.pointerCount == 1) {
                        lastX = event.x; lastY = event.y; dragging = true
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        // Continue dragging with the remaining finger without a jump.
                        val i = if (event.actionIndex == 0) 1 else 0
                        lastX = event.getX(i); lastY = event.getY(i)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        dragging = false
                        v.performClick()
                    }
                }
            } else {
                dragging = false
            }
            true
        }
    }

    /** Scales the image to fit the screen and places it in the exact center. */
    private fun fitImageToCenter() {
        val b = _binding ?: return
        val drawable = b.imageView.drawable ?: return
        val vw = b.imageView.width.toFloat()
        val vh = b.imageView.height.toFloat()
        if (vw == 0f || vh == 0f) return

        val dw = drawable.intrinsicWidth.toFloat()
        val dh = drawable.intrinsicHeight.toFloat()
        baseScale = minOf(vw / dw, vh / dh) * 0.9f // small margin around the image
        currentScale = baseScale

        matrix.reset()
        matrix.postScale(baseScale, baseScale)
        matrix.postTranslate((vw - dw * baseScale) / 2f, (vh - dh * baseScale) / 2f)
        b.imageView.imageMatrix = matrix
    }

    /** Keeps the image from being dragged completely off the screen. */
    private fun applyMatrix() {
        val b = _binding ?: return
        val drawable = b.imageView.drawable ?: return
        val vw = b.imageView.width.toFloat()
        val vh = b.imageView.height.toFloat()

        val rect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        matrix.mapRect(rect)

        var dx = 0f
        var dy = 0f
        // Horizontal
        if (rect.width() <= vw) dx = (vw - rect.width()) / 2f - rect.left
        else if (rect.left > 0) dx = -rect.left
        else if (rect.right < vw) dx = vw - rect.right
        // Vertical
        if (rect.height() <= vh) dy = (vh - rect.height()) / 2f - rect.top
        else if (rect.top > 0) dy = -rect.top
        else if (rect.bottom < vh) dy = vh - rect.bottom
        matrix.postTranslate(dx, dy)

        b.imageView.imageMatrix = matrix
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
