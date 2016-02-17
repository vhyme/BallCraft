package cn.vhyme.ballcraft.ui;

import android.content.Context;

import cn.vhyme.ballcraft.GameView;

public class PlayerBall extends Ball {

    public float speedX = 0, speedY = 0;

    public PlayerBall(Context context, float x, float y, float radius) {
        super(context, x, y, radius);
    }

    public float getSpeedFactor(){
        return (radius <= GameView.DEFAULT_SIZE ? 1 :
                1 / (float) (Math.log(radius) / Math.log(GameView.DEFAULT_SIZE)))
                * GameView.BASE_SPEED_FACTOR;
    }

    public void move(int worldW, int worldH) {
        if(radius < GameView.SUGAR_SIZE) radius = 0;
        float speedFactor = getSpeedFactor();
        x += speedX * speedFactor;
        y += speedY * speedFactor;
        if (x < radius) {
            x = radius;
            speedY = (speedY < 0 ? -1 : 1) * (float)Math.sqrt(speedX * speedX + speedY * speedY);
            speedX = 0;
        } else if (x > worldW - radius) {
            x = worldW - radius;
            speedY = (speedY < 0 ? -1 : 1) * (float)Math.sqrt(speedX * speedX + speedY * speedY);
            speedX = 0;
        }
        if (y < radius) {
            y = radius;
            speedX = (speedX < 0 ? -1 : 1) * (float)Math.sqrt(speedX * speedX + speedY * speedY);
            speedY = 0;
        } else if (y > worldH - radius) {
            y = worldH - radius;
            speedX = (speedX < 0 ? -1 : 1) * (float)Math.sqrt(speedX * speedX + speedY * speedY);
            speedY = 0;
        }
    }

    public void transact(Ball ball){
        float d = radius + ball.radius;
        if(Math.abs(x - ball.x) > d || Math.abs(y - ball.y) > d) return;
        if(Math.sqrt((x - ball.x) * (x - ball.x) + (y - ball.y) * (y - ball.y)) > d) return;
        if(ball != this && radius > ball.radius){
            if(ball.radius <= GameView.SUGAR_SIZE) {
                float r1 = ball.radius - GameView.SUGAR_SIZE;
                radius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius - r1 * r1);
                ball.radius = r1;
                if (ball.radius < GameView.SUGAR_SIZE) ball.radius = 0;
            } else {
                float r1 = ball.radius - GameView.BASE_SPEED_FACTOR;
                radius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius - r1 * r1);
                ball.radius = r1;
                if (ball.radius < GameView.BASE_SPEED_FACTOR) ball.radius = 0;
            }
        } else if (ball != this && radius == ball.radius){
            float dx = x - ball.x;
            float dy = y - ball.y;
            float module = (float)Math.sqrt(dx * dx + dy * dy);
            if(module != 0){
                x += dx / module * GameView.BASE_SPEED_FACTOR;
                y += dy / module * GameView.BASE_SPEED_FACTOR;
            }
        }
    }
}
