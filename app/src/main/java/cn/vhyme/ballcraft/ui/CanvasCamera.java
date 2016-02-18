package cn.vhyme.ballcraft.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import cn.vhyme.ballcraft.GameView;

public class CanvasCamera {

    private int fullWidth, fullHeight, canvasWidth, canvasHeight;

    private int cameraX, cameraY;

    private Context context;

    private Canvas canvas;

    public enum ScaleType {
        FIT_INNER, FIT_OUTER
    }

    private ScaleType cameraScaleType = ScaleType.FIT_OUTER;

    public static final int EXPECTED_CAMERA_WIDTH = 320,
            EXPECTED_CAMERA_HEIGHT = 480;

    public float scaleFactor = 1, zoomFactor;

    public CanvasCamera(Context context, int fullWidth, int fullHeight) {
        this.context = context;
        this.fullWidth = fullWidth;
        this.fullHeight = fullHeight;
    }

    public CanvasCamera initialize(Canvas canvas, float focusX, float focusY, float zoomFactor) {
        this.canvas = canvas;
        this.zoomFactor = zoomFactor;
        this.canvasWidth = canvas.getWidth();
        this.canvasHeight = canvas.getHeight();

        float widthScaleFactor = canvasWidth / (EXPECTED_CAMERA_WIDTH / zoomFactor);
        float heightScaleFactor = canvasHeight / (EXPECTED_CAMERA_HEIGHT / zoomFactor);

        switch (cameraScaleType) {
            case FIT_INNER:
                scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
                break;
            case FIT_OUTER:
                scaleFactor = Math.max(widthScaleFactor, heightScaleFactor);
                break;
        }

        cameraX = (int) (focusX * scaleFactor) - canvasWidth / 2;
        cameraY = (int) (focusY * scaleFactor) - canvasHeight / 2;
        return this;
    }

    public CanvasCamera outerBg(int color) {
        canvas.drawColor(color);
        return this;
    }

    public CanvasCamera innerBg(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        Rect rect = new Rect(
                Math.max(-cameraX, 0),
                Math.max(-cameraY, 0),
                Math.min((int) (fullWidth * scaleFactor) - cameraX, canvasWidth),
                Math.min((int) (fullHeight * scaleFactor) - cameraY, canvasHeight)
        );
        canvas.drawRect(rect, paint);
        return this;
    }

    public CanvasCamera drawDividers(int interval, int strokeWidth, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        int leftMax = Math.max(-cameraX, 0);
        int topMax = Math.max(-cameraY, 0);
        int rightMax = Math.min((int) (fullWidth * scaleFactor) - cameraX, canvasWidth);
        int bottomMax = Math.min((int) (fullHeight * scaleFactor) - cameraY, canvasHeight);
        for (float pos = 0; pos < GameView.WORLD_WIDTH * scaleFactor; pos += interval * scaleFactor) {
            if (cameraX < pos && pos < cameraX + canvasWidth) {
                canvas.drawLine(pos - cameraX, topMax, pos - cameraX, bottomMax, paint);
            }
        }
        for (float pos = 0; pos < GameView.WORLD_HEIGHT * scaleFactor; pos += interval * scaleFactor) {
            if (cameraY < pos && pos < cameraY + canvasHeight) {
                canvas.drawLine(leftMax, pos - cameraY, rightMax, pos - cameraY, paint);
            }
        }
        return this;
    }

    public CanvasCamera drawBall(float x, float y, float radius, Paint paint) {
        float realX = x * scaleFactor - cameraX;
        float realY = y * scaleFactor - cameraY;
        float realR = radius * scaleFactor;
        paint.setStrokeWidth(paint.getStrokeWidth() * scaleFactor);
        if (realX + realR > 0 && realX - realR < canvasWidth
                && realY + realR > 0 && realY - realR < canvasHeight)
            canvas.drawCircle(realX, realY, realR, paint);
        return this;
    }
}
