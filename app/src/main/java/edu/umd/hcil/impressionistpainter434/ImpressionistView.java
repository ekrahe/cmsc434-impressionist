package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private PreviewImageView _imageView;

    private Canvas _offScreenCanvas = null;
    protected Bitmap _offScreenBitmap = null, _picture = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private long _lastPointTime = -1;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Circle;
    private float _minBrushRadius = 60;

    private Rect _bounds = null;

    private ArrayList<LotsOfTouches> _undoStack, _redoStack;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(_minBrushRadius);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        _undoStack = new ArrayList<>();
        _redoStack = new ArrayList<>();

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    private class ForgetMENot {

        public ArrayList<Float> xs, ys;
        public float x, y;

        public ForgetMENot(MotionEvent me) {
            xs = new ArrayList<>();
            ys = new ArrayList<>();
            x = me.getX();
            y = me.getY();
            for (int i = 0; i < me.getHistorySize(); i++) {
                xs.add(me.getHistoricalX(i));
                ys.add(me.getHistoricalY(i));
            }
        }

    }

    private class LotsOfTouches {

        ArrayList<ForgetMENot> touches;
        BrushType brush;
        ArrayList<Float> scales;

        public LotsOfTouches(BrushType bt) {
            brush = bt;
            touches = new ArrayList<>();
            scales = new ArrayList<>();
        }

        public void addTouch(ForgetMENot fmn, float size) {
            touches.add(fmn);
            scales.add(size);
        }

    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if (bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
            clearPainting();
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(PreviewImageView imageView) {
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType) {
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting() {
        if (_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
            _undoStack.clear();
            _redoStack.clear();
        }
        _bounds = null;
        invalidate();
    }

    private void internalClear() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
    }

    public boolean undo() {
        if(_undoStack.size() > 0) {
            _redoStack.add(_undoStack.remove(_undoStack.size() - 1));
            internalClear();
            for (LotsOfTouches lot : _undoStack) {
                BrushType bt = lot.brush;
                if (bt == BrushType.Circle || bt == BrushType.SpeedCircle) {
                    _paint.setStrokeCap(Paint.Cap.ROUND);
                } else {
                    _paint.setStrokeCap(Paint.Cap.SQUARE);
                }

                for (int i = 0; i < lot.touches.size(); i++) {
                    _paint.setStrokeWidth(_minBrushRadius * lot.scales.get(i));
                    redrawPoints(lot.touches.get(i));
                }
            }
            invalidate();
            if (_undoStack.size() == 0) return true;
            return false;
        }
        return true;
    }

    public boolean redo() {
        if(_redoStack.size() > 0) {
            LotsOfTouches lot = _redoStack.remove(_redoStack.size() - 1);
            _undoStack.add(lot);

            BrushType bt = lot.brush;
            if (bt == BrushType.Circle || bt == BrushType.SpeedCircle) {
                _paint.setStrokeCap(Paint.Cap.ROUND);
            } else {
                _paint.setStrokeCap(Paint.Cap.SQUARE);
            }

            for (int i = 0; i < lot.touches.size(); i++) {
                _paint.setStrokeWidth(_minBrushRadius * lot.scales.get(i));
                redrawPoints(lot.touches.get(i));
            }
            invalidate();
            if (_redoStack.size() == 0) return true;
            return false;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, null);
            if (_imageView != null) _bounds = getBitmapPositionInsideImageView(_imageView);
            canvas.drawRect(_bounds, _paintBorder);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        if (!_redoStack.isEmpty()) _redoStack.clear();

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _undoStack.add(new LotsOfTouches(_brushType));
            case MotionEvent.ACTION_MOVE:
                setDrawingPaint(motionEvent);
                drawPoints(motionEvent);
                _lastPointTime = SystemClock.elapsedRealtime();
                break;
            case MotionEvent.ACTION_UP:
                _lastPointTime = -1;
                _imageView.setPreviewPoint(-1, -1, -1);
                break;
        }

        return true;
    }

    private void setDrawingPaint(MotionEvent motionEvent) {
        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();

        if (_brushType == BrushType.Circle || _brushType == BrushType.SpeedCircle) {
            _paint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            _paint.setStrokeCap(Paint.Cap.SQUARE);
        }

        if (_lastPointTime != -1 &&
                (_brushType == BrushType.SpeedCircle || _brushType == BrushType.SpeedSquare)) {
            long elapsedTime = SystemClock.elapsedRealtime() - _lastPointTime;

            int history = motionEvent.getHistorySize();
            float sx = history > 0 ? motionEvent.getHistoricalX(0) : curTouchX;
            float sy = history > 0 ? motionEvent.getHistoricalY(0) : curTouchY;

            double dist = Math.sqrt((curTouchX - sx) * (curTouchX - sx) + (curTouchY - sy) * (curTouchY - sy));
            float scaleFactor = Math.min(1 + (float) (dist / elapsedTime), 6f);
            _paint.setStrokeWidth(_minBrushRadius * scaleFactor);
            _undoStack.get(_undoStack.size() - 1).addTouch(new ForgetMENot(motionEvent), scaleFactor);
        } else {
            _paint.setStrokeWidth(_minBrushRadius);
            _undoStack.get(_undoStack.size() - 1).addTouch(new ForgetMENot(motionEvent), 1);
        }
    }

    private void drawPoints(MotionEvent motionEvent) {
        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();

        // For efficiency, motion events with ACTION_MOVE may batch together multiple movement samples within a single object.
        // The most current pointer coordinates are available using getX(int) and getY(int).
        // Earlier coordinates within the batch are accessed using getHistoricalX(int, int) and getHistoricalY(int, int).
        // See: http://developer.android.com/reference/android/view/MotionEvent.html
        int historySize = motionEvent.getHistorySize();

        if (_offScreenBitmap != null) {
            for (int i = 0; i < historySize; i++) {

                float touchX = motionEvent.getHistoricalX(i);
                float touchY = motionEvent.getHistoricalY(i);

                if (_bounds.contains((int) touchX, (int) touchY)) {
                    _paint.setColor(_picture.getPixel((int) touchX - _bounds.left,
                            (int) touchY - _bounds.top));
                    _paint.setAlpha(_alpha);
                    _offScreenCanvas.drawPoint(touchX, touchY, _paint);
                }
            }

            if (_bounds.contains((int) curTouchX, (int) curTouchY)) {
                _paint.setColor(_picture.getPixel((int) curTouchX - _bounds.left,
                        (int) curTouchY - _bounds.top));
                _paint.setAlpha(_alpha);
                _offScreenCanvas.drawPoint(curTouchX, curTouchY, _paint);

                _imageView.setPreviewPoint((int) curTouchX, (int) curTouchY, _paint.getColor());
            } else {
                _imageView.setPreviewPoint(-1, -1, -1);
            }
            invalidate();

        }

        //long endTime = SystemClock.elapsedRealtime();
        //_elapsedTimeProcessingTouchEventsInMs += endTime - startTime;
    }

    private void redrawPoints(ForgetMENot fmn) {
        float curTouchX = fmn.x;
        float curTouchY = fmn.y;

        // For efficiency, motion events with ACTION_MOVE may batch together multiple movement samples within a single object.
        // The most current pointer coordinates are available using getX(int) and getY(int).
        // Earlier coordinates within the batch are accessed using getHistoricalX(int, int) and getHistoricalY(int, int).
        // See: http://developer.android.com/reference/android/view/MotionEvent.html
        int historySize = fmn.xs.size();

        if (_offScreenBitmap != null) {
            for (int i = 0; i < historySize; i++) {

                float touchX = fmn.xs.get(i);
                float touchY = fmn.ys.get(i);

                if (_bounds.contains((int) touchX, (int) touchY)) {
                    _paint.setColor(_picture.getPixel((int) touchX - _bounds.left,
                            (int) touchY - _bounds.top));
                    _paint.setAlpha(_alpha);
                    _offScreenCanvas.drawPoint(touchX, touchY, _paint);
                }
            }

            if (_bounds.contains((int) curTouchX, (int) curTouchY)) {
                _paint.setColor(_picture.getPixel((int) curTouchX - _bounds.left,
                        (int) curTouchY - _bounds.top));
                _paint.setAlpha(_alpha);
                _offScreenCanvas.drawPoint(curTouchX, curTouchY, _paint);
            }
            invalidate();
        }

        //long endTime = SystemClock.elapsedRealtime();
        //_elapsedTimeProcessingTouchEventsInMs += endTime - startTime;
    }

    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView) {
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    public void setPicture(Bitmap bmp) {
        clearPainting();
        _bounds = getBitmapPositionInsideImageView(_imageView);
        _picture = Bitmap.createScaledBitmap(bmp, _bounds.width(), _bounds.height(), true);
    }
}

