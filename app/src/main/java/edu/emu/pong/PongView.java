package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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

    private void createEntities() {
        float ballWidth = (displayWidth < displayHeight ? displayWidth : displayHeight) / 11;
        Ball ball = new Ball(0, 0, ballWidth, ballWidth, 1, 1);
    }

    private void updateEntities(float delta) {
        for (Entity entity: Entity.getInstances()) {
            if (onXWall(entity)) {
                entity.setRightwardVelocityScale(-1 * entity.getRightwardVelocityScale());
            } else if (onYWall(entity)) {
                entity.setDownwardVelocityScale(-1 * entity.getDownwardVelocityScale());
            }
            entity.setX(adjustPosition(entity, entity.getX() + (delta * entity.getRightwardVelocity()), true));
            entity.setY(adjustPosition(entity, entity.getY() + (delta * entity.getDownwardVelocity()), false));
        }
    }

    private float adjustPosition(Entity entity, float targetPosition, boolean xCoordinate) {
        float position = xCoordinate ? entity.getX() : entity.getY();
        float minimumBound = 0;
        float maximumBound = xCoordinate ? displayWidth : displayHeight;
        float adjustedPosition = targetPosition;
        float offset = (xCoordinate ? entity.getWidth() : entity.getHeight());
        if (targetPosition < minimumBound) {
            adjustedPosition = minimumBound;
        } else if (targetPosition + offset > maximumBound) {
            adjustedPosition = maximumBound - offset;
        }
        return adjustedPosition;
    }

    public boolean onXWall(Entity entity) {
        return entity.getX() == 0 || entity.getX() == displayWidth - entity.getWidth();
    }

    public boolean onYWall(Entity entity) {
        return entity.getY() == 0 || entity.getY() == displayHeight - entity.getHeight();
    }
}