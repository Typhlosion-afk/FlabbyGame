package com.example.flabbygame.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.example.flabbygame.R
import com.example.flabbygame.game_obj.ColorSet
import com.example.flabbygame.game_obj.Column

class PreviewView(context: Context?, attrs: AttributeSet) : View(context, attrs) {
    @ColorInt
    private var colorBird: Int = 0

    @ColorInt
    private var colorColumn: Int = 0

    @ColorInt
    private var colorBackground: Int = 0

    private var birdPaint = Paint()
    private var columnPaint = Paint()
    private var backgroundPaint = Paint()

    private var listColumn = ArrayList<Column>()

    private var viewHeight: Int = 0
    private var viewWidth: Int = 0

    private var columnWidth = 0f
    private var columnSpace = 0f
    private var spaceHeight = 0f
    private var birdRadius = 0f

    private val listSpace = listOf(0.3f,0.7f,0.5f)

    init {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.GamePreview)
        colorBird =
            a?.getColor(R.styleable.GamePreview_bird_color_review, Color.BLUE) ?: Color.BLUE
        colorColumn =
            a?.getColor(R.styleable.GamePreview_column_color_review, Color.BLUE) ?: Color.BLUE
        colorBackground =
            a?.getColor(R.styleable.GamePreview_background_color_review, Color.WHITE) ?: Color.WHITE

        initPaint()
        a?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        viewWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        setMeasuredDimension(viewWidth, viewHeight)

        columnWidth = 0.08f * viewWidth
        columnSpace = 0.19f * viewWidth
        spaceHeight = 0.3f * viewHeight
        birdRadius = 0.04f * viewHeight

        initColumns()
    }

    private fun initColumns() {
        listColumn.clear()
        var curX = viewWidth * 0.4f
        var pos = 0
        while (curX < viewWidth) {
            listColumn.add(
                Column(curX, viewHeight * listSpace[pos%3])
            )
            pos++
            curX += columnWidth + columnSpace
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawBird(canvas)
        drawListColumn(canvas)
    }

    private fun initPaint() {
        with(backgroundPaint) {
            style = Paint.Style.FILL
            color = colorBackground
            isAntiAlias = true
        }

        with(birdPaint) {
            style = Paint.Style.FILL
            color = colorBird
            isAntiAlias = true
        }

        with(columnPaint) {
            style = Paint.Style.FILL
            color = colorColumn
            isAntiAlias = true
        }
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawColor(colorBackground)
    }

    private fun drawListColumn(canvas: Canvas?) {
        if (listColumn.isNotEmpty()) {
            for (i in listColumn.indices) {
                drawColumn(canvas, listColumn[i])
            }
        }
    }

    private fun drawBird(canvas: Canvas?) {
        canvas?.drawCircle(
            10f * viewWidth / 100f,
            viewHeight / 2f,
            birdRadius,
            birdPaint
        )
    }

    private fun drawColumn(canvas: Canvas?, column: Column) {
        canvas?.drawRect(
            column.spaceX - columnWidth / 2f,
            column.spaceY + spaceHeight / 2f,
            column.spaceX + column.width / 2f,
            viewHeight.toFloat(),
            columnPaint
        )

        canvas?.drawRect(
            column.spaceX - columnWidth / 2f,
            0f,
            column.spaceX + column.width / 2f,
            column.spaceY - spaceHeight / 2f,
            columnPaint
        )
    }

    fun setColor(colorSet: ColorSet) {
        colorBird = colorSet.bird
        colorBackground = colorSet.background
        colorColumn = colorSet.column

        initPaint()
        invalidate()
    }
}