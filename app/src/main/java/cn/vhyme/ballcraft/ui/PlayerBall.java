package cn.vhyme.ballcraft.ui;

import android.content.Context;

import java.util.List;
import java.util.Vector;

import cn.vhyme.ballcraft.GameView;

public class PlayerBall extends MotionBall {

    private GameView view;

    protected List<Ball> balls;

    public PlayerBall(Context context, GameView view, float x, float y, float radius) {
        super(context, x, y, radius);
        this.view = view;
    }

    public void updateBallList(Vector<Ball> balls){
        this.balls = balls;
    }

    @Override
    public void move(int worldW, int worldH) {
        if(!(this instanceof NPCBall)) {
            for (Ball ball : balls) {
                if (ball instanceof PlayerBall && !(ball instanceof NPCBall) &&
                        ((PlayerBall) ball).moved && moved) {
                    // TODO 写排斥算法
                }
            }
        }
        super.move(worldW, worldH);
    }

    // 返回值 是否吃掉了一个NPC
    public boolean eat(Ball ball) {

        if (!scaled || ball.eaten) return false;

        float d = radius - ball.radius * .2f;

        if (Math.abs(x - ball.x) > d || Math.abs(y - ball.y) > d) return false;

        if (Math.sqrt((x - ball.x) * (x - ball.x) + (y - ball.y) * (y - ball.y)) > d) return false;

        if (ball instanceof MotionBall && !((MotionBall) ball).scaled) return false;

        if (ball instanceof MotionBall && !((MotionBall) ball).moved) return false;

        if (ball != this
                && ball instanceof PlayerBall
                && !(ball instanceof NPCBall) && !(this instanceof NPCBall)) { // 都是自己的球

            if ((this.radius > ball.radius
                    || this.radius == ball.radius && this.hashCode() < ball.hashCode()) // 强调合并的主宾关系防止主宾混淆
                    && moved // 发射分身或吐球的过程中防止合并
                    ) {
                // 合并
                scaled = false;
                scaledRadius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius);
                ball.eaten = true;
            }

        } else if (ball != this
                && radius / ball.radius > 1 / (1 + GameView.IGNORED_DIFF_RATIO)
                && radius / ball.radius < (1 + GameView.IGNORED_DIFF_RATIO)) {
            return false;
        } else if (ball != this && radius > ball.radius) {
            // 对方被吃掉了
            scaled = false;
            scaledRadius = (float) Math.sqrt(radius * radius + ball.radius * ball.radius);
            ball.eaten = true;
            return ball instanceof NPCBall;
        }
        return false;
    }
}
