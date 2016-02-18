package cn.vhyme.ballcraft.ui;

import android.content.Context;

import cn.vhyme.ballcraft.GameView;

public class MotionBall extends Ball {

    public float speedX = 0, speedY = 0, scaledRadius, leftDX, leftDY, targetDX, targetDY;

    public boolean scaled = false, moved = true;

    public int playerToken;

    public MotionBall(Context context, float x, float y, float radius) {
        super(context, x, y, GameView.SUGAR_SIZE);
        playerToken = hashCode();
        scaledRadius = radius;
    }

    public float getRealRadius(){
        return scaled ? radius : scaledRadius;
    }

    public void scaleTo(float radius) {
        scaled = false;
        scaledRadius = radius;
    }

    public void stopScale(){
        scaled = true;
        radius = scaledRadius;
    }

    public void moveBy(float dx, float dy) {
        moved = false;
        leftDX = targetDX = dx;
        leftDY = targetDY = dy;
    }

    public float getSpeedFactor() {
        return (radius <= GameView.DEFAULT_SIZE ? 1 :
                1 / (float) (Math.log(radius) / Math.log(GameView.DEFAULT_SIZE)))
                * GameView.BASE_SPEED_FACTOR;
    }

    public void move(int worldW, int worldH) {

        // 新球产生或大小变化的动画
        if (!scaled) {
            if (scaledRadius > radius) {// 放大
                if (radius >= scaledRadius - GameView.BASE_SPEED_FACTOR) {
                    radius = scaledRadius;
                    scaled = true;
                } else {
                    radius += GameView.BASE_SPEED_FACTOR;
                }
            } else {// 缩小
                if (radius <= scaledRadius + GameView.BASE_SPEED_FACTOR) {
                    radius = scaledRadius;
                    scaled = true;
                } else {
                    radius -= GameView.BASE_SPEED_FACTOR / 2;
                }
            }
        }

        float speedFactor = getSpeedFactor();

        // 新分身产生时的定向移动动画
        float additionalSpeedX = 0, additionalSpeedY = 0;
        if (!moved) {
            float dx = leftDX;
            float dy = leftDY;
            float module = (float) Math.sqrt(dx * dx + dy * dy);
            float friction = (targetDX != 0 ? leftDX / targetDX : leftDY / targetDY) * .5f + .1f;
            if (module > 0) {
                additionalSpeedX = dx / module * GameView.BASE_SPEED_FACTOR * radius * friction;
                additionalSpeedY = dy / module * GameView.BASE_SPEED_FACTOR * radius * friction;

                if (!(this instanceof FeedBall)) {
                    additionalSpeedX /= 2;
                    additionalSpeedY /= 2;
                }
                leftDX -= additionalSpeedX;
                leftDY -= additionalSpeedY;
            }
            if (module <= getSpeedFactor()) {
                moved = true;
            }
        }

        if (radius < GameView.SUGAR_SIZE) radius = 0;
        x += (speedX + additionalSpeedX) * speedFactor;
        y += (speedY + additionalSpeedY) * speedFactor;
        if (x < radius) {
            x = radius;
            speedX = 0;
        } else if (x > worldW - radius) {
            x = worldW - radius;
            speedX = 0;
        }
        if (y < radius) {
            y = radius;
            speedY = 0;
        } else if (y > worldH - radius) {
            y = worldH - radius;
            speedY = 0;
        }
    }
}
