package com.smartpack.packagemanager.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.ViewUtils;

public class BorderCircleView extends FrameLayout {

    public static final SparseArray<String> sAccentColors = new SparseArray<>();

    private final Drawable mCheck;
    private final Paint mPaint;
    private final Paint mPaintBorder;

    public BorderCircleView(Context context) {
        this(context, null);
    }

    public BorderCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isClickable()) {
            setForeground(ViewUtils.getSelectableBackground(context));
        }
        mCheck = ContextCompat.getDrawable(context, R.drawable.ic_done);
        DrawableCompat.setTint(mCheck, Color.WHITE);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ViewUtils.getThemeAccentColor(context));

        mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorder.setColor(ViewUtils.getColorPrimaryColor(context));
        mPaintBorder.setStrokeWidth((int) getResources().getDimension(R.dimen.circleview_border));
        mPaintBorder.setStyle(Paint.Style.STROKE);

        setWillNotDraw(false);
    }

    @Override
    public void setBackgroundColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        float radius = Math.min(width, height) / 2f - 4f;

        canvas.drawCircle(width / 2, height / 2, radius, mPaint);
        canvas.drawCircle(width / 2, height / 2, radius, mPaintBorder);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float desiredWidth = getResources().getDimension(R.dimen.circleview_width);
        float desiredHeight = getResources().getDimension(R.dimen.circleview_height);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float width;
        float height;

        if (widthMode == MeasureSpec.EXACTLY) width = widthSize;
        else if (widthMode == MeasureSpec.AT_MOST) width = Math.min(desiredWidth, widthSize);
        else width = desiredWidth;

        if (heightMode == MeasureSpec.EXACTLY) height = heightSize;
        else if (heightMode == MeasureSpec.AT_MOST) height = Math.min(desiredHeight, heightSize);
        else height = desiredHeight;

        setMeasuredDimension((int) width, (int) height);
    }
}
