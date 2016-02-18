package cn.vhyme.ballcraft.ui;

import android.content.Context;

import cn.vhyme.ballcraft.GameView;

public class FeedBall extends MotionBall {
    public FeedBall(Context context, float x, float y, float radius) {
        super(context, x, y, GameView.SUGAR_SIZE);
        scaledRadius = radius;
    }
}