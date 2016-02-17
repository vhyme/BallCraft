package cn.vhyme.ballcraft.ui;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import cn.vhyme.ballcraft.GameView;
import cn.vhyme.ballcraft.R;

public class Ball {

    public float x, y, radius;

    public int color;

    public boolean eaten = false;

    public static final int[] COLOR = {
            R.color.i, R.color.j, R.color.k, R.color.l, R.color.m, R.color.n,
            R.color.o, R.color.p, R.color.q, R.color.r, R.color.s, R.color.t
    };

    public Ball(Context context, float x, float y, float radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = ContextCompat.getColor(context, COLOR[(int)(Math.random()*COLOR.length - 1)]);
    }

    public void draw(CanvasCamera camera){
        if(eaten){
            radius -= GameView.BASE_SPEED_FACTOR;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        camera.drawBall(x, y, radius, paint);
    }
}
