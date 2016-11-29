package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class PongView extends View {

    private static final int FRAMES_PER_SECOND = 60;
    private static final int TICKS_PER_SECOND = 80;
    private static final int TICK_LENGTH = 1000000000 / TICKS_PER_SECOND;
    private float positionDelta;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Handler frameHandler = new Handler();
    private Runnable frameRefresher = new Runnable(){
        public void run() {
            invalidate();
        }
    };
    private final Handler physicsHandler = new Handler();
    private final Runnable physicsUpdater = new Runnable() {
        @Override
        public void run() {
            long currentLoopTime = System.nanoTime();
            long updateLength = currentLoopTime - lastLoopTime;
            lastLoopTime = currentLoopTime;
            float delta = updateLength / (float) TICK_LENGTH;
            long start = System.nanoTime();
            updateEntities(delta);
            physicsHandler.postDelayed(physicsUpdater, (TICK_LENGTH - System.nanoTime() - currentLoopTime) / 1000000);
        }
    };
    private long lastLoopTime;
    private int displayWidth;
    private int displayHeight;
    private Paddle paddle;
    private Ball ball;
    private boolean started = false;

    public PongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayHeight = metrics.heightPixels;
        createEntities();
        physicsHandler.post(physicsUpdater);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Entity entity : Entity.getInstances()) {
            entity.draw(canvas, paint);
        }
        frameHandler.postDelayed(frameRefresher, 1000 / FRAMES_PER_SECOND);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean leftSide = event.getX() < displayWidth / 2;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!started) {
                    started = true;
                    ball.setDownwardVelocityScale(1);
                    ball.setRightwardVelocityScale(1);
                }
                if (leftSide) {
                    paddle.setRightMovement(false);
                    paddle.setLeftMovement(true);
                } else {
                    paddle.setLeftMovement(false);
                    paddle.setRightMovement(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (leftSide) {
                    paddle.setLeftMovement(false);
                } else {
                    paddle.setRightMovement(false);
                }
                break;
        }
        return true;
    }

    private void createEntities() {
        //float longestDimension = displayWidth > displayHeight ? displayWidth : displayHeight;
        float paddleWidth = displayWidth / 4;
        float paddleHeight = displayWidth / 60;
        float paddleX = (displayWidth / 2) - (paddleWidth / 2);
        float paddleY = displayHeight - paddleHeight;
        paddle = new Paddle(paddleX, paddleY, paddleWidth, paddleHeight);
        float ballWidth = displayWidth / 20.5f;
        ball = new Ball(0, 0, ballWidth, ballWidth, 0, 0);
    }

    private void updateEntities(float delta) {
        paddle.setX(adjustPosition(paddle, paddle.getX() + (delta * paddle.getRightwardVelocity()), true));
        for (Entity entity: Entity.getInstances()) {
            if (!entity.equals(paddle)) {
                entity.setX(adjustPosition(entity, entity.getX() + (delta * entity.getRightwardVelocity()), true));
                entity.setY(adjustPosition(entity, entity.getY() + (delta * entity.getDownwardVelocity()), false));
                if (entity instanceof Ball) {
                    if (onWall(entity)) {
                        entity.setRightwardVelocityScale(-1 * entity.getRightwardVelocityScale());
                    } else if (onCeiling(entity) || onPaddle(entity) || onFloor(entity)) {
                        entity.setDownwardVelocityScale(-1 * entity.getDownwardVelocityScale());
                    }
                }
            }
        }
    }

    private float adjustPosition(Entity entity, float targetPosition, boolean xCoordinate) {
        float position = xCoordinate ? entity.getX() : entity.getY();
        float minimumBound = 0;
        float maximumBound = xCoordinate ? displayWidth : displayHeight + entity.getHeight() + 100;
        float adjustedPosition = targetPosition;
        float offset = (xCoordinate ? entity.getWidth() : entity.getHeight());
        if (targetPosition < minimumBound) {
            adjustedPosition = minimumBound;
            System.out.println("Under min bound");
        } else if (targetPosition + offset > maximumBound) {
            //System.out.println("Exceeded max bound");
            adjustedPosition = maximumBound - offset;
        }
        if (entity instanceof Ball && !xCoordinate && inPaddleDomain(entity) &&
                entity.getY() > displayHeight - entity.getHeight() - paddle.getHeight() &&
                entity.getY() < displayHeight - entity.getHeight()) {
            System.out.println("Exceeded paddle bound");
            adjustedPosition = displayHeight - entity.getHeight() - paddle.getHeight();
        }
        return adjustedPosition;
    }

    public boolean onWall(Entity entity) {
        return entity.getX() == 0 || entity.getX() == displayWidth - entity.getWidth();
    }

    public boolean onCeiling(Entity entity) {
        return entity.getY() <= 0;
    }

    public boolean onFloor(Entity entity) {
        return entity.getY() == displayHeight + 100;
    }

    public boolean inPaddleDomain(Entity entity) {
        return paddle.getX() - entity.getWidth() < entity.getX() && entity.getX() < paddle.getX() + paddle.getWidth();
    }

    public boolean onPaddle(Entity entity) {
        return inPaddleDomain(entity) && entity.getY() == displayHeight - entity.getHeight() - paddle.getHeight();
    }
}