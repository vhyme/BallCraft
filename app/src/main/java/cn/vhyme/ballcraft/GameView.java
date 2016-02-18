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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;
import java.util.Vector;

import cn.vhyme.ballcraft.ui.Ball;
import cn.vhyme.ballcraft.ui.CanvasCamera;
import cn.vhyme.ballcraft.ui.FeedBall;
import cn.vhyme.ballcraft.ui.MotionBall;
import cn.vhyme.ballcraft.ui.NPCBall;
import cn.vhyme.ballcraft.ui.PlayerBall;
import cn.vhyme.ballcraft.ui.VirusBall;

public class GameView extends View {

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private CanvasCamera camera;

    private boolean finalized = false;

    private float density;

    public static final int WORLD_WIDTH = 1000, WORLD_HEIGHT = 1000,
            GAME_MINUTES = 5, REFRESH_INTERVAL = 20,
            DEFAULT_SIZE = 10, SUGAR_SIZE = 3, FEED_SIZE = 6, VIRUS_SIZE = 20, FEED_DISTANCE = 50,
            SUGAR_COUNT = 120, NPC_COUNT = 20, VIRUS_COUNT = 20,
            MAX_SPLITS = 16, MERGE_DELAY_SECONDS = 10;

    public static final float BASE_SPEED_FACTOR = 1.3f, MOTION_SPEED_FACTOR = 3f,
            IGNORED_DIFF_RATIO = .05f;

    private Paint textPaint;

    private Vector<PlayerBall> myBalls = new Vector<>();

    private Vector<Ball> balls = new Vector<>();

    private int maxMass, eatenCount;

    private float[] zoom;

    private long surviveTime;

    public long lastSplit = 0;

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

        myBalls.add(new PlayerBall(getContext(), this,
                (int) (Math.random() * WORLD_WIDTH),
                (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE));
        balls.add(myBalls.get(0));

        for (int i = 0; i < SUGAR_COUNT; i++) {
            addSugar();
        }
     /*   for (int i = 0; i < VIRUS_COUNT; i++) {
            addVirus();
        }*/
        for (int i = 0; i < NPC_COUNT; i++) {
            balls.add(new NPCBall(getContext(), this,
                    (int) (Math.random() * WORLD_WIDTH),
                    (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE));
        }
        camera = new CanvasCamera(getContext(), WORLD_WIDTH, WORLD_HEIGHT);
    }

    public void addSugar() {
        balls.add(new Ball(getContext(),
                (int) (Math.random() * WORLD_WIDTH),
                (int) (Math.random() * WORLD_HEIGHT), SUGAR_SIZE));
    }

    public void addVirus() {
        balls.add(new VirusBall(getContext(),
                (int) (Math.random() * WORLD_WIDTH),
                (int) (Math.random() * WORLD_HEIGHT), VIRUS_SIZE));
    }

    private float[] getZoomFactorAndFocus() {
        Ball ball = myBalls.get(0);
        float minX = ball.x - ball.radius;
        float minY = ball.y - ball.radius;
        float maxX = ball.x + ball.radius;
        float maxY = ball.y + ball.radius;
        for (int i = 1; i < myBalls.size(); i++) {
            ball = myBalls.get(i);
            if (ball.x - ball.radius < minX) minX = ball.x - ball.radius;
            if (ball.x + ball.radius > maxX) maxX = ball.x + ball.radius;
            if (ball.y - ball.radius < minY) minY = ball.y - ball.radius;
            if (ball.y + ball.radius > maxY) maxY = ball.y + ball.radius;
        }
        float r = Math.max(maxX - minX,
                (maxY - minY) / CanvasCamera.EXPECTED_CAMERA_HEIGHT * CanvasCamera.EXPECTED_CAMERA_WIDTH);
        return new float[]{
                r < 100 ? 1 : ((2 / (float) Math.log10(r) - 1) * 3 + 1),
                (maxX + minX) / 2,
                (maxY + minY) / 2
        };
    }

    private int getPlayerMass() {
        int mass = 0;
        for (PlayerBall ball : myBalls) {
            float radius = ball.scaled ? ball.radius : ball.scaledRadius;
            mass += radius * radius * Math.PI;
        }
        return mass;
    }

    public void split() {
        Vector<PlayerBall> newBalls = new Vector<>();

        for (PlayerBall oldBall : myBalls) {
            if (myBalls.size() < MAX_SPLITS
                    && oldBall.radius >= DEFAULT_SIZE * (float) Math.sqrt(2)) {
                float oldRadius = oldBall.scaled ? oldBall.radius : oldBall.scaledRadius;
                float newRadius = oldRadius / (float) Math.sqrt(2);
                oldBall.scaleTo(newRadius);
                float dx = oldBall.speedX;
                float dy = oldBall.speedY;
                float module = (float) Math.sqrt(dx * dx + dy * dy);
                if (module > 0) {
                    dx /= module;
                    dy /= module;
                    module = newRadius * 3;
                    dx *= module;
                    dy *= module;
                }
                PlayerBall newBall = new PlayerBall(getContext(), this, oldBall.x, oldBall.y, oldRadius);
                newBall.scaleTo(newRadius);
                newBall.moveBy(dx, dy);

                newBall.color = oldBall.color;
                newBall.playerToken = oldBall.playerToken;
                newBalls.add(newBall);
            }
        }

        if (newBalls.size() > 0)
            lastSplit = System.currentTimeMillis();

        for (PlayerBall newBall : newBalls) {
            myBalls.add(newBall);
            balls.add(newBall);
        }
    }

    public void feed() {
        float minSizeSq = DEFAULT_SIZE * DEFAULT_SIZE + FEED_SIZE * FEED_SIZE;
        for (PlayerBall ball : myBalls) {
            if (ball.radius * ball.radius >= minSizeSq) {
                ball.scaleTo((float) Math.sqrt(ball.radius * ball.radius - FEED_SIZE * FEED_SIZE));
                FeedBall ball1 = new FeedBall(getContext(), ball.x, ball.y, FEED_SIZE);
                ball1.color = ball.color;
                ball1.playerToken = ball.playerToken;
                balls.add(ball1);
                float dx = ball.speedX;
                float dy = ball.speedY;
                float module = (float) Math.sqrt(dx * dx + dy * dy);
                if (module == 0) continue;
                dx /= module;
                dy /= module;
                dx *= (ball.radius + FEED_DISTANCE);
                dy *= (ball.radius + FEED_DISTANCE);
                ball1.moveBy(dx, dy);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (myBalls.size() > 0) zoom = getZoomFactorAndFocus();
        camera.initialize(canvas, zoom[1], zoom[2], zoom[0])
                .outerBg(Color.BLACK)
                .innerBg(ContextCompat.getColor(getContext(), R.color.worldBg))
                .drawDividers(10, 2, ContextCompat.getColor(getContext(), R.color.divider));

        for (Ball ball : balls) {
            ball.draw(camera);
        }

        textPaint.setTextAlign(Paint.Align.LEFT);
        int mass = getPlayerMass();
        canvas.drawText(mass + "千克", 7 * density, 19 * density, textPaint);
        if (mass > maxMass) maxMass = mass;

        textPaint.setTextAlign(Paint.Align.RIGHT);
        int seconds = GAME_MINUTES * 60 - (int) (surviveTime / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        canvas.drawText("剩余时间：" + minutes + ":" + (seconds < 10 ? "0" : "") + seconds,
                getWidth() - 7 * density, 19 * density, textPaint);

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
                    if (time < REFRESH_INTERVAL) {
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
        if (surviveTime >= GAME_MINUTES * 60 * 1000) {
            finalized = true;
            if (maxMass > sp.getInt("massRecord", 0)) {
                editor.putInt("massRecord", maxMass);
                editor.apply();
            }
            if (eatenCount > sp.getInt("eatenRecord", 0)) {
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
                ((PlayerBall) ball).updateBallList(balls);
            }
            if (ball instanceof MotionBall) {
                ((MotionBall) ball).move(WORLD_WIDTH, WORLD_HEIGHT);
            }
            for (Ball ball1 : balls) {
                if (ball1 instanceof PlayerBall) {
                    boolean enemyEaten = ((PlayerBall) ball1).eat(ball);
                    if (!(ball1 instanceof NPCBall) && enemyEaten) {
                        eatenCount++;
                    }
                }
            }
            // 垃圾球回收
            if (ball.radius <= 0) {
                balls.remove(ball);
                if (ball instanceof NPCBall) {
                    balls.add(new NPCBall(getContext(), this,
                            (int) (Math.random() * WORLD_WIDTH), (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE));
                } else if (ball instanceof PlayerBall) {
                    myBalls.remove(ball);
                    if (myBalls.size() < 1) {
                        if (maxMass > sp.getInt("massRecord", 0)) {
                            editor.putInt("massRecord", maxMass);
                            editor.apply();
                        }
                        if (eatenCount > sp.getInt("eatenRecord", 0)) {
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
                                    PlayerBall ball1 = new PlayerBall(getContext(), this,
                                            (int) (Math.random() * WORLD_WIDTH),
                                            (int) (Math.random() * WORLD_HEIGHT), DEFAULT_SIZE);
                                    myBalls.add(ball1);
                                    balls.add(ball1);
                                })
                                .setNegativeButton("重新开始", (dlg, which) -> {
                                    ((Activity) getContext()).recreate();
                                })
                                .show();
                    }
                } else if (ball instanceof FeedBall){

                } else {
                    addSugar();
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
                if (myBalls.size() > 0) zoom = getZoomFactorAndFocus();
                float x = zoom[1] + (event.getX() - getWidth() / 2) / camera.scaleFactor;
                float y = zoom[2] + (event.getY() - getHeight() / 2) / camera.scaleFactor;

                for (PlayerBall ball : myBalls) {
                    float dx = x - ball.x;
                    float dy = y - ball.y;
                    float module = (float) Math.sqrt(dx * dx + dy * dy);

                    ball.speedX = dx / module * ball.getSpeedFactor();
                    ball.speedY = dy / module * ball.getSpeedFactor();
                }
        }
        return true;
    }
}
