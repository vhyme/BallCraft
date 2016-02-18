package cn.vhyme.ballcraft.ui;

import android.content.Context;

import java.util.List;
import java.util.Vector;

import cn.vhyme.ballcraft.GameView;


public class NPCBall extends PlayerBall {


    public static final int WALL_FORCE = 60;

    // 迟钝系数
    private float delayFactor;

    public NPCBall(Context context, GameView view, float x, float y, float radius) {
        super(context, view, x, y, radius);
        delayFactor = (float) Math.random() / 2;
    }

    @Override
    public void move(int worldW, int worldH) {
        if (balls.size() > 0) {

            // 对NPC的运动算法进行重构
            // 使用模拟力学的方案
            float vx = 0, vy = 0, totalModule = 0;
            for (Ball ball : balls) {
                // 发现大小相同的球，对不同的球随机选择“不做处理”或“当作敌人”
                if (radius / ball.radius > 1 / (1 + GameView.IGNORED_DIFF_RATIO)
                        && radius / ball.radius < (1 + GameView.IGNORED_DIFF_RATIO)) {
                    if (ball.radius < radius || ball.hashCode() % 2 == 0) continue;
                }

                if (ball.radius < radius) {
                    // 发现食物，产生引力
                    float distanceSquare = (ball.x - x) * (ball.x - x) + (ball.y - y) * (ball.y - y);
                    if (distanceSquare == 0) continue;
                    float module = ball.radius * ball.radius / distanceSquare;
                    float x1 = (ball.x - x);
                    float y1 = (ball.y - y);
                    float module2 = (float) Math.sqrt(x1 * x1 + y1 * y1);
                    vx += module * x1 / module2;
                    vy += module * y1 / module2;
                    totalModule += module;
                } else {
                    // 发现敌人，产生斥力
                    float distanceSquare = (ball.x - x) * (ball.x - x) + (ball.y - y) * (ball.y - y);
                    if (distanceSquare == 0) continue;
                    float module = ball.radius * ball.radius / distanceSquare;
                    float x1 = (ball.x - x);
                    float y1 = (ball.y - y);
                    float module2 = (float) Math.sqrt(x1 * x1 + y1 * y1);
                    vx -= module * x1 / module2;
                    vy -= module * y1 / module2;
                    totalModule -= module;
                }
            }

            // 若被追杀，产生对墙的斥力
            if (totalModule < 0) {
                if (x > 0) {
                    float module = WALL_FORCE / (x * x);
                    vx += module * x;
                }
                if (worldW - x > 0) {
                    float module = WALL_FORCE / ((worldW - x) * (worldW - x));
                    vx += module * (x - worldW);
                }
                if (y > 0) {
                    float module = WALL_FORCE / (y * y);
                    vy += module * y;
                }
                if (worldH - y > 0) {
                    float module = WALL_FORCE / ((worldH - y) * (worldH - y));
                    vy += module * (y - worldH);
                }
            }

            // 部分NPC会很迟钝，取决于随机生成的迟钝系数
            vx *= delayFactor;
            vy *= delayFactor;
            speedX += vx;
            speedY += vy;
            float module = (float) Math.sqrt(speedX * speedX + speedY * speedY);
            if (module != 0) {
                speedX = speedX / module * getSpeedFactor();
                speedY = speedY / module * getSpeedFactor();
            }
        }
        super.move(worldW, worldH);
    }
}
