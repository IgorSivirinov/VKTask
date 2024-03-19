package com.oneandzero.vktask

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class WatchView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {
    companion object {
        private const val FullCircle = 2*PI.toFloat()
    }

    private var seconds: Int = 0
    private var minutes: Int = 0
    private var hours: Int = 0

    fun setTime(hours: Int, minutes: Int, seconds: Int){
        if (hours !in 0..23)
            throw IllegalArgumentException("The variable must take a value in period 0..23")
        if (minutes !in 0..59)
            throw IllegalArgumentException("The variable must take a value in period 0..59")
        if (seconds !in 0..59)
            throw IllegalArgumentException("The variable must take a value in period 0..59")

        this.hours = if(hours > 11) hours - 12 else hours
        this.minutes = minutes
        this.seconds = seconds

        invalidate()
    }

    private var displayRadius = 0f
    private var verticalPadding = 0f
    private var horizontalPadding = 0f

    private val markPainter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val bigMarkPainter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 5f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 256.toPx()
        val desiredHeight = 256.toPx()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        displayRadius = (min(w, h) / 2).toFloat()

        horizontalPadding = (w / 2) - displayRadius
        verticalPadding = (h / 2) - displayRadius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fun getX(radius: Float, corner: Float): Float = radius * cos(corner) + displayRadius + horizontalPadding
        fun getY(radius: Float, corner: Float): Float =  radius * sin(corner) + displayRadius + verticalPadding

        val markLength = displayRadius / 10
        val markStartRadius = (displayRadius * 10) / 12
        val textStartRadius = (displayRadius * 10) / 14


        fun Canvas.drawMarks() {
            val parts = 12*5

            repeat(parts) {
                val corner = (it * FullCircle) / parts

                drawLine(
                    getX(markStartRadius, corner),
                    getY(markStartRadius, corner),
                    getX(markStartRadius + markLength, corner),
                    getY(markStartRadius + markLength, corner),
                    markPainter
                )
            }
        }

        fun Canvas.drawBigMarks() {
            val parts = 12

            repeat(parts) {
                val corner = (it * FullCircle) / parts

                drawLine(
                    getX(markStartRadius, corner),
                    getY(markStartRadius, corner),
                    getX(markStartRadius + markLength, corner),
                    getY(markStartRadius + markLength, corner),
                    bigMarkPainter
                )
            }
        }

        fun Canvas.drawNumbers(){
            val parts = 12
            val textHeight = displayRadius / 10
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
                textSize = textHeight
            }

            repeat(parts) {
                val corner = ((it - 2) * FullCircle) / parts

                drawText(
                    (it+1).toString(),
                    getX(textStartRadius, corner),
                    getY(textStartRadius, corner) - (textPaint.descent() + textPaint.ascent()) / 2,
                    textPaint
                )
            }
        }

        fun Canvas.drawSecondLine(){
            val parts = 60
            val lineLength = markStartRadius
            val lainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.RED

                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 5f
            }

            val corner = ((seconds - 15) * FullCircle) / parts

            drawLine(
                getX(0f, 0f),
                getY(0f, 0f),
                getX(lineLength, corner),
                getY(lineLength, corner),
                lainPaint
            )
        }

        fun Canvas.drawMinutesLine(){
            val parts = 360
            val lineLength = (displayRadius * 8) / 12
            val lainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.BLACK

                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 5f
            }

            val corner = ((seconds  + minutes*60 - 90) * FullCircle) / parts

            drawLine(
                getX(0f, 0f),
                getY(0f, 0f),
                getX(lineLength, corner),
                getY(lineLength, corner),
                lainPaint
            )
        }

        fun Canvas.drawHoursLine(){
            val parts = 43_200
            val lineLength = (displayRadius * 7) / 12
            val lainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.BLACK

                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 9f
            }

            val corner = ((seconds + minutes*60 + hours*60*60 - 10_800) * FullCircle) / parts

            drawLine(
                getX(0f, 0f),
                getY(0f, 0f),
                getX(lineLength, corner),
                getY(lineLength, corner),
                lainPaint
            )
        }

        canvas.apply {
            drawMarks()
            drawBigMarks()
            drawNumbers()

            drawHoursLine()
            drawMinutesLine()
            drawSecondLine()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()

        bundle.putInt("seconds", seconds)
        bundle.putInt("minutes", minutes)
        bundle.putInt("hours", hours)
        bundle.putParcelable("instanceState", super.onSaveInstanceState())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle

        seconds = bundle.getInt("seconds")
        minutes = bundle.getInt("minutes")
        hours = bundle.getInt("hours")

        super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
    }
}