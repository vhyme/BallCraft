package cn.vhyme.ballcraft.ui;

import android.content.Context;

import cn.vhyme.ballcraft.GameView;

public class PlayerBall extends Ball {

    public float speedX = 0, speedY = 0, preparedRadius;

    public long lastSplit = 0;

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
        // 新球产生或大小变化的动画
        if (!prepared) {
            if (preparedRadius > radius) {
                if (radius >= preparedRadius - GameView.BASE_SPEED_FACTOR) {
                    radius = preparedRadius;
                    prepared = true;
                } else {
                    radius += GameView.BASE_SPEED_FACTOR;
                }
            } else {
                if (radius <= preparedRadius + GameView.BASE_SPEED_FACTOR) {
                    radius = preparedRadius;
                    prepared = true;
                } else {
                    radius -= GameView.BASE_SPEED_FACTOR;
                }
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

    // 返回值 是否吃掉了一个NPC
    public boolean eat(Ball ball) {
        if (!prepared || ball.eaten) return false;

        float d = radius - ball.radius * .2f;

        if (Math.abs(x - ball.x) > d || Math.abs(y - ball.y) > d) return false;

        if (Math.sqrt((x - ball.x) * (x - ball.x) + (y - ball.y) * (y - ball.y)) > d) return false;

        if (ball instanceof PlayerBall && !((PlayerBall) ball).prepared) return false;

        if (ball != this
                && ball instanceof PlayerBall
                && this.hashCode() < ball.hashCode() // 强调合并的主宾关系防止主宾混淆
                && !(ball instanceof NPCBall) && !(this instanceof NPCBall)) {
            // 都是自己的球，合并
            prepared = false;
            preparedRadius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius);
            ball.eaten = true;
        }

        if (ball != this
                && radius / ball.radius > 1 / (1 + GameView.IGNORED_DIFF_RATIO)
                && radius / ball.radius < (1 + GameView.IGNORED_DIFF_RATIO)) {
            return false;
        } else if (ball != this && radius > ball.radius) {
            // 对方被吃掉了
            prepared = false;
            preparedRadius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius);
            ball.eaten = true;
            return ball instanceof NPCBall;
        }
        return false;
    }
}
