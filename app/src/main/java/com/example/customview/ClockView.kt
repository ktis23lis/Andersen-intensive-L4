package com.example.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("Recycle")
class ClockView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val clockFacePaint: Paint
    private val minuteLinePaint: Paint
    private var secondLinePaint: Paint
    private val hourLinePaint: Paint
    private val centerPaint: Paint
    private val lineZonePaint: Paint

    companion object {
        const val UPDATE = 500L
        const val CIRCLE_WIDTH = 25f
        const val LINE_WIDTH = 15f
        const val SECOND_WIDTH_DEF = 5f
        const val MINUTE_WIDTH_DEF = 12f
        const val HOUR_WIDTH_DEF = 20f
        const val CENTER_RADIUS = 20f
        const val LINE_ROTATE = 30f
        const val PADDING = 50
        const val AMOUNT_HOUR = 12
        const val HALF = 2
        const val QUARTER = 4
        const val COMPLETE_DIAL_CYCLE = 60.0
        const val INTERMEDIATE_HOUR = 5f
        const val ANGLE_MIN_OF_PI = 30
    }

    private val rangeHour = 1..12
    private var isInit = false
    private var mAngle = 0.0
    private var mMin = 0
    private var handTruncation = 0
    private var hourHandTruncation = 0
    private var radius = 0
    private var myCenterX = 0
    private var myCenterY = 0
    private var myHeight = 0
    private var myWidth = 0
    private var secondHandColor = 0
    private var minuteHandColor = 0
    private var hourHandColor = 0
    private var secondHandSize = 0
    private var minuteHandSize = 0
    private var hourHandSize = 0

    init {

        context.obtainStyledAttributes(
                attrs,
                R.styleable.ClockView).apply {
            try {
                secondHandColor = getColor(R.styleable.ClockView_second_hand_color, Color.RED)
                minuteHandColor = getColor(R.styleable.ClockView_minute_hand_color, Color.BLACK)
                hourHandColor = getColor(R.styleable.ClockView_hour_hand_color, Color.BLACK)
                secondHandSize = getDimensionPixelSize(R.styleable.ClockView_second_hand_size, SECOND_WIDTH_DEF.toInt())
                minuteHandSize = getDimensionPixelSize(R.styleable.ClockView_minute_hand_size, MINUTE_WIDTH_DEF.toInt())
                hourHandSize = getDimensionPixelSize(R.styleable.ClockView_hour_hand_size, HOUR_WIDTH_DEF.toInt())
            } finally {
                recycle()
            }
        }

        clockFacePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = CIRCLE_WIDTH
            style = Paint.Style.STROKE
        }
        lineZonePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = LINE_WIDTH
        }
        secondLinePaint = Paint().apply {
            color = secondHandColor
            strokeWidth = secondHandSize.toFloat()
        }
        minuteLinePaint = Paint().apply {
            color = minuteHandColor
            strokeWidth = minuteHandSize.toFloat()
        }
        hourLinePaint = Paint().apply {
            color = hourHandColor
            strokeWidth = hourHandSize.toFloat()
        }
        centerPaint = Paint().apply {
            color = Color.BLACK
        }
    }

    private fun initClock() {
        myHeight = height
        myWidth = width

        myCenterX = myWidth / HALF
        myCenterY = myHeight / HALF

        mMin = myHeight.coerceAtMost(myWidth)
        radius = mMin / HALF - PADDING

        mAngle = (Math.PI / ANGLE_MIN_OF_PI) - (Math.PI / HALF)

        hourHandTruncation = radius - radius / HALF
        handTruncation = radius - radius / QUARTER
        isInit = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInit) {
            initClock()
        }
        drawCircle(canvas)
        postInvalidateDelayed(UPDATE)
    }

    private fun drawCircle(canvas: Canvas?) {
        canvas?.drawCircle(
                myCenterX.toFloat(),
                myCenterY.toFloat(),
                radius.toFloat(),
                clockFacePaint
        )
        drawZoneLine(canvas)
        drawHands(canvas)
        drewCenter(canvas)
    }

    private fun drawHands(canvas: Canvas?) {
        val calendar = Calendar.getInstance()
        val mHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (mHour > AMOUNT_HOUR) mHour - AMOUNT_HOUR else mHour
        val mMinute = calendar.get(Calendar.MINUTE)
        val mSecond = calendar.get(Calendar.SECOND)

        drawHourLine(canvas, (mHour + mMinute / COMPLETE_DIAL_CYCLE) * INTERMEDIATE_HOUR)
        drawMinuteLine(canvas, mMinute.toDouble())
        drawSecondLine(canvas, mSecond.toDouble())
    }

    private fun drawSecondLine(canvas: Canvas?, double: Double) {
        createAngle(double)
        canvas?.drawLine(
                myCenterX.toFloat(),
                myCenterY.toFloat(),
                (myCenterX + cos(mAngle) * handTruncation).toFloat(),
                (myCenterY + sin(mAngle) * handTruncation).toFloat(),
                secondLinePaint
        )
    }

    private fun drawMinuteLine(canvas: Canvas?, double: Double) {
        createAngle(double)
        canvas?.drawLine(
                myCenterX.toFloat(),
                myCenterY.toFloat(),
                (myCenterX + cos(mAngle) * handTruncation).toFloat(),
                (myCenterY + sin(mAngle) * handTruncation).toFloat(),
                minuteLinePaint
        )
    }

    private fun drawHourLine(canvas: Canvas?, double: Double) {
        createAngle(double)
        canvas?.drawLine(
                myCenterX.toFloat(),
                myCenterY.toFloat(),
                (myCenterX + cos(mAngle) * hourHandTruncation).toFloat(),
                (myCenterY + sin(mAngle) * hourHandTruncation).toFloat(),
                hourLinePaint
        )
    }

    private fun drawZoneLine(canvas: Canvas?) {
        for (i in rangeHour) {
            canvas?.rotate(LINE_ROTATE, myCenterX.toFloat(), myCenterY.toFloat())
            canvas?.drawLine(
                    myCenterX.toFloat(),
                    (myCenterY + radius).toFloat(),
                    myCenterX.toFloat(),
                    myCenterY.toFloat() + radius - PADDING,
                    lineZonePaint)
        }
    }

    private fun drewCenter(canvas: Canvas?) {
        canvas?.drawCircle(
                myCenterX.toFloat(),
                myCenterY.toFloat(),
                CENTER_RADIUS,
                centerPaint
        )
    }

    private fun createAngle(double: Double) {
        mAngle = Math.PI * double / ANGLE_MIN_OF_PI - Math.PI / HALF
    }

    fun setSecondHandColor(color: Int) {
        secondLinePaint.color = color
        postInvalidate()
    }

    fun setMinuteHandColor(color: Int) {
        minuteLinePaint.color = color
        postInvalidate()
    }

    fun setHourHandColor(color: Int) {
        hourLinePaint.color = color
        postInvalidate()
    }

    fun setSecondHandSize(size: Float) {
        secondLinePaint.strokeWidth = size
        postInvalidate()
    }

    fun setMinuteHandSize(size: Float) {
        minuteLinePaint.strokeWidth = size
        postInvalidate()
    }

    fun setHourHandSize(size: Float) {
        hourLinePaint.strokeWidth = size
        postInvalidate()
    }
}