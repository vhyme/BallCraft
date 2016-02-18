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

        if (ball.eaten) return false;

        float realRadius = getRealRadius();

        float d = realRadius - ball.radius * .2f;

        if (Math.abs(x - ball.x) > d || Math.abs(y - ball.y) > d) return false;

        if (Math.sqrt((x - ball.x) * (x - ball.x) + (y - ball.y) * (y - ball.y)) > d) return false;

        if (ball instanceof MotionBall && !((MotionBall) ball).scaled) return false;

        if (ball instanceof MotionBall && !((MotionBall) ball).moved) return false;

        if (ball != this
                && ball instanceof PlayerBall && ((PlayerBall) ball).playerToken == playerToken) { // 都是自己的球

            if ((realRadius > ball.radius
                    || realRadius == ball.radius && this.hashCode() < ball.hashCode()) // 强调合并的主宾关系防止主宾混淆
                    && moved // 发射分身或吐球的过程中防止合并
                    ) {
                // 合并
                scaleTo((float) Math.sqrt(realRadius * realRadius + ball.radius * ball.radius));
                ball.eaten = true;
                ((PlayerBall) ball).scaleTo(0);
            }

        } else if (ball != this
                && realRadius / ball.radius > 1 / (1 + GameView.IGNORED_DIFF_RATIO)
                && realRadius / ball.radius < (1 + GameView.IGNORED_DIFF_RATIO)) {
            return false;
        } else if (ball instanceof VirusBall) {
            scaleTo((float) Math.sqrt(realRadius * realRadius + ball.radius * ball.radius));
            ball.eaten = true;
            // TODO 爆炸
        } else if (ball != this && realRadius > ball.radius) {
            // 对方被吃掉了
            scaleTo((float) Math.sqrt(realRadius * realRadius + ball.radius * ball.radius));
            ball.eaten = true;
            return ball instanceof NPCBall;
        }
        return false;
    }
}
