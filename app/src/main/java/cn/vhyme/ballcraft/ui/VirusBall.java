package cn.vhyme.ballcraft.ui;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.support.v4.content.ContextCompat;

import cn.vhyme.ballcraft.GameView;
import cn.vhyme.ballcraft.R;

public class VirusBall extends MotionBall {
    public VirusBall(Context context, float x, float y, float radius) {
        super(context, x, y, radius);
        color = ContextCompat.getColor(context, R.color.n);
    }

    @Override
    public void draw(CanvasCamera camera) {
        if (eaten) {
            radius -= GameView.BASE_SPEED_FACTOR;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setPathEffect(new PathDashPathEffect(makePath((int) Math.ceil(radius * camera.scaleFactor)),
                10, 0, PathDashPathEffect.Style.ROTATE));
        paint.setStrokeWidth(3*camera.scaleFactor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        camera.drawBall(x, y, radius, paint);
    }

    private Path makePath(int radius){
        Path path = new Path();
        path.moveTo(-5, 10);
        path.lineTo(0, radius+10);
        path.lineTo(5, 10);
        path.lineTo(0, -5);
        path.close();
        return path;
    }
}
