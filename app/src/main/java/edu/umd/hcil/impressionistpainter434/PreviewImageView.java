package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Eddie on 4/3/2016.
 */
public class PreviewImageView extends ImageView {

    private Point _previewPoint = null;

    private Paint _previewPaint = new Paint(), _borderPaint = new Paint();

    public PreviewImageView(Context context) {
        super(context);
        init();
    }

    public PreviewImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        _borderPaint.setColor(Color.BLACK);
        _borderPaint.setStrokeWidth(75);
        _borderPaint.setStyle(Paint.Style.STROKE);
        _borderPaint.setStrokeCap(Paint.Cap.ROUND);

        _previewPaint.setStrokeWidth(60);
        _previewPaint.setStyle(Paint.Style.STROKE);
        _previewPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (_previewPoint != null) {
            canvas.drawPoint(_previewPoint.x, _previewPoint.y, _borderPaint);
            canvas.drawPoint(_previewPoint.x, _previewPoint.y, _previewPaint);
        }
    }

    public void setPreviewPoint(int x, int y, int color) {
        if (x < 0) _previewPoint = null;
        else {
            _previewPoint = new Point(x, y);
            _previewPaint.setColor(color);
            _previewPaint.setAlpha(255);

            float[] hsv = {0,0,0};
            Color.colorToHSV(color, hsv);
            if(hsv[2] < 0.5f) _borderPaint.setColor(Color.WHITE);
            else _borderPaint.setColor(Color.BLACK);
        }
        invalidate();
    }

}
