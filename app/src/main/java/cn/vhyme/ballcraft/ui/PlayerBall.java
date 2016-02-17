package cn.vhyme.ballcraft.ui;

import android.content.Context;

import cn.vhyme.ballcraft.GameView;

public class PlayerBall extends Ball {

    public float speedX = 0, speedY = 0, preparedRadius;

    public boolean prepared = false;

    public PlayerBall(Context context, float x, float y, float radius) {
        super(context, x, y, GameView.SUGAR_SIZE);
        preparedRadius = radius;
    }

    public float getSpeedFactor() {
        return (radius <= GameView.DEFAULT_SIZE ? 1 :
                1 / (float) (Math.log(radius) / Math.log(GameView.DEFAULT_SIZE)))
                * GameView.BASE_SPEED_FACTOR;
    }

    public void move(int worldW, int worldH) {
        // 新球产生的动画
        if (!prepared) {
            if (radius >= preparedRadius - GameView.BASE_SPEED_FACTOR) {
                radius = preparedRadius;
                prepared = true;
            } else {
                radius += GameView.BASE_SPEED_FACTOR;
            }
        }
        if (radius < GameView.SUGAR_SIZE) radius = 0;
        float speedFactor = getSpeedFactor();
        x += speedX * speedFactor;
        y += speedY * speedFactor;
        if (x < radius) {
            x = radius;
            speedY = (speedY < 0 ? -1 : 1) * (float) Math.sqrt(speedX * speedX + speedY * speedY);
            speedX = 0;
        } else if (x > worldW - radius) {
            x = worldW - radius;
            speedY = (speedY < 0 ? -1 : 1) * (float) Math.sqrt(speedX * speedX + speedY * speedY);
            speedX = 0;
        }
        if (y < radius) {
            y = radius;
            speedX = (speedX < 0 ? -1 : 1) * (float) Math.sqrt(speedX * speedX + speedY * speedY);
            speedY = 0;
        } else if (y > worldH - radius) {
            y = worldH - radius;
            speedX = (speedX < 0 ? -1 : 1) * (float) Math.sqrt(speedX * speedX + speedY * speedY);
            speedY = 0;
        }
    }

    // 返回值 是否吃光了一个NPC
    public boolean transact(Ball ball) {
        float d = radius + ball.radius;
        if (Math.abs(x - ball.x) > d || Math.abs(y - ball.y) > d) return false;
        if (Math.sqrt((x - ball.x) * (x - ball.x) + (y - ball.y) * (y - ball.y)) > d) return false;
        if (ball instanceof PlayerBall && !((PlayerBall) ball).prepared) return false;
        if (ball != this
                && radius / ball.radius > 1 / (1 + GameView.IGNORED_DIFF_RATIO)
                && radius / ball.radius < (1 + GameView.IGNORED_DIFF_RATIO)) {
            float dx = x - ball.x;
            float dy = y - ball.y;
            float module = (float) Math.sqrt(dx * dx + dy * dy);
            if (module != 0) {
                x += dx / module * GameView.BASE_SPEED_FACTOR;
                y += dy / module * GameView.BASE_SPEED_FACTOR;
            }
        } else if (ball != this && radius > ball.radius) {
            if (ball.radius <= GameView.SUGAR_SIZE) {
                // 对方被吃光了
                float r1 = ball.radius - GameView.SUGAR_SIZE;
                radius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius - r1 * r1);
                ball.radius = r1;
                if (ball.radius < GameView.SUGAR_SIZE) ball.radius = 0;
                return ball instanceof NPCBall;
            } else {
                float r1 = ball.radius - GameView.BASE_SPEED_FACTOR;
                radius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius - r1 * r1);
                ball.radius = r1;
                if (ball.radius < GameView.BASE_SPEED_FACTOR) ball.radius = 0;
            }
        }
        return false;
    }
}
