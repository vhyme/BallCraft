package cn.vhyme.ballcraft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;
import java.util.Vector;

import cn.vhyme.ballcraft.ui.Ball;
import cn.vhyme.ballcraft.ui.CanvasCamera;
import cn.vhyme.ballcraft.ui.NPCBall;
import cn.vhyme.ballcraft.ui.PlayerBall;

public class GameView extends View {

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private CanvasCamera camera;

    private boolean finalized = false;

    private float density;

    public static final int WORLD_WIDTH = 1200, WORLD_HEIGHT = 1200,
            GAME_MINUTES = 5, REFRESH_INTERVAL = 20,
            DEFAULT_SIZE = 10, SUGAR_SIZE = 3,
            SUGAR_COUNT = 100, NPC_COUNT = 20;

    public static final float BASE_SPEED_FACTOR = 1.3f,
            IGNORED_DIFF_RATIO = .1f;

    private Paint textPaint;

    private PlayerBall playerBall;

    private Vector<Ball> balls = new Vector<>();

    private int maxMass, eatenCount;

    private long surviveTime;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
        density = context.getResources().getDisplayMetrics().density;
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14 * density);
        initialize();
    }

    private void initialize() {
        maxMass = 0;
        surviveTime = 0;
        eatenCount = 0;

        balls = new Vector<>();

        playerBall = new PlayerBall(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE);
        for (int i = 0; i < SUGAR_COUNT; i++) {
            balls.add(new Ball(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), SUGAR_SIZE));
        }
        for (int i = 0; i < NPC_COUNT; i++) {
            balls.add(new NPCBall(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE));
        }
        balls.add(playerBall);
        camera = new CanvasCamera(getContext(), WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float zoomFactor = playerBall.radius < 100 ? 1 :
                ((2 / (float) Math.log10(playerBall.radius) - 1) * 3 + 1);

        camera.initialize(canvas, playerBall.x, playerBall.y, zoomFactor)
                .outerBg(Color.BLACK)
                .innerBg(ContextCompat.getColor(getContext(), R.color.worldBg))
                .drawDividers(10, 2, ContextCompat.getColor(getContext(), R.color.divider));
        for (Ball ball : balls) {
            ball.draw(camera);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText((int) (playerBall.radius * playerBall.radius * Math.PI) + "千克",
                7 * density, 19 * density, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        int seconds = GAME_MINUTES * 60 - (int)(surviveTime / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        canvas.drawText("剩余时间：" + minutes + ":" + (seconds < 10 ? "0" : "") + seconds,
                getWidth() - 7 * density, 19 * density, textPaint);
        int mass = (int) (playerBall.radius * playerBall.radius * Math.PI);
        if (mass > maxMass) maxMass = mass;

        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        new Thread(() -> {
            for (; ; ) {
                if (finalized) return;
                long time = System.currentTimeMillis();
                refreshCanvas.run();
                time = System.currentTimeMillis() - time;
                try {
                    if(time < REFRESH_INTERVAL) {
                        Thread.sleep(REFRESH_INTERVAL - time);
                        surviveTime += REFRESH_INTERVAL;
                    } else {
                        surviveTime += time;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private LambdaHandler refreshCanvas = new LambdaHandler(() -> {
        Collections.sort(balls, (ball1, ball2) -> new Float(ball1.radius).compareTo(ball2.radius));
        invalidate();

        // 时间到
        if (surviveTime >= GAME_MINUTES * 60 * 1000){
            finalized = true;
            if(maxMass > sp.getInt("massRecord", 0)){
                editor.putInt("massRecord", maxMass);
                editor.putInt("eatenRecord", eatenCount);
                editor.apply();
            }
            new AlertDialog.Builder(getContext()).setMessage("本局结束\n" +
                    "最大体重：" + maxMass + "千克（纪录" + sp.getInt("massRecord", 0) + "千克）\n" +
                    "吞噬球数：" + eatenCount + "（纪录" + sp.getInt("eatenRecord", 0) + "）\n")
                    .setCancelable(true)
                    .setOnCancelListener((dlg) -> {
                        ((Activity) getContext()).finish();
                    })
                    .setNegativeButton("再来一局", (dlg, which) -> {
                        ((Activity) getContext()).recreate();
                    })
                    .show();
        }
        for (int i = 0; i < balls.size(); ) {
            Ball ball = balls.get(i);
            if (ball instanceof PlayerBall) {
                if (ball instanceof NPCBall) {
                    ((NPCBall) ball).updateBallList(balls);
                }
                ((PlayerBall) ball).move(WORLD_WIDTH, WORLD_HEIGHT);
            }
            for (Ball ball1 : balls) {
                if (ball1 instanceof PlayerBall) {
                    boolean enemyEatenUp = ((PlayerBall) ball1).transact(ball);
                    if(!(ball1 instanceof NPCBall) && enemyEatenUp){
                        eatenCount++;
                    }
                }
            }

            // 垃圾球回收
            if (ball.radius <= 0) {
                balls.remove(ball);
                if (ball instanceof NPCBall) {
                    balls.add(new NPCBall(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE));
                } else if (ball instanceof PlayerBall) {
                    if(maxMass > sp.getInt("massRecord", 0)){
                        editor.putInt("massRecord", maxMass);
                        editor.apply();
                    }
                    if(eatenCount > sp.getInt("eatenRecord", 0)){
                        editor.putInt("eatenRecord", eatenCount);
                        editor.apply();
                    }
                    new AlertDialog.Builder(getContext()).setMessage("你被吃掉了！\n" +
                            "最大体重：" + maxMass + "千克（纪录" + sp.getInt("massRecord", 0) + "千克）\n" +
                            "吞噬球数：" + eatenCount + "（纪录" + sp.getInt("eatenRecord", 0) + "）\n" +
                            "存活时间：" + ((surviveTime / 1000 / 60) == 0 ? "" : (surviveTime / 1000 / 60) + "分")
                            + (surviveTime / 1000 % 60) + "秒\n")
                            .setCancelable(true)
                            .setOnCancelListener((dlg) -> {
                                ((Activity) getContext()).finish();
                            })
                            .setPositiveButton("继续本局", (dlg, which) -> {
                                eatenCount = 0;
                                balls.remove(playerBall);
                                playerBall = new PlayerBall(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE);
                                balls.add(playerBall);
                            })
                            .setNegativeButton("重新开始", (dlg, which) -> {
                                ((Activity) getContext()).recreate();
                            })
                            .show();
                } else {
                    balls.add(new Ball(getContext(), (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), SUGAR_SIZE));
                }
            } else {
                i++;
            }
        }
    });

    @Override
    protected void onDetachedFromWindow() {
        finalized = true;
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - getWidth() / 2;
                float dy = event.getY() - getHeight() / 2;
                float module = (float) Math.sqrt(dx * dx + dy * dy);
                playerBall.speedX = dx / module * playerBall.getSpeedFactor();
                playerBall.speedY = dy / module * playerBall.getSpeedFactor();
        }
        return true;
    }
}
