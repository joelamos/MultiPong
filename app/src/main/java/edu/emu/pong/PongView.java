package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
    private List<Ball> balls = new ArrayList<Ball>();
    private Ball startBall;
    private boolean started = false;
    private int playerId = -1;
    private int players;

    public PongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayHeight = metrics.heightPixels;
        createEntities();
        physicsHandler.post(physicsUpdater);
    }

    public void initialize(int playerId, int players) {
        this.playerId = playerId;
        this.players = players;
    }

    public void start(int secondsToStart) {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (startBall != null) {
                            startBall.setDownwardVelocityScale(rand(.8, 1.2));
                            startBall.setRightwardVelocityScale(rand(.8, 1.2));
                        }
                    }
                }, secondsToStart * 1000
        );
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    private void sendBall(Ball ball) {
        float[] deviceInfo = getDeviceEntranceInfo(ball);
        int device = (int) deviceInfo[0];
        int ballNum = getBallNum(ball);
        float relativeX = deviceInfo[1];
        float dxScale = deviceInfo[2];
        float dycale = deviceInfo[3];
        // TODO actually send the ball
    }

    private void receiveBall(int ballNum, float relativeX, float dxScale, float dyScale) {
        Ball ball = balls.get(ballNum);
        ball.setX(relativeX * displayWidth);
        ball.setDownwardVelocityScale(dyScale);
        ball.setRightwardVelocityScale(dxScale);
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
        float paddleWidth = displayWidth / 4;
        float paddleHeight = displayWidth / 60;
        float paddleX = (displayWidth / 2) - (paddleWidth / 2);
        float paddleY = displayHeight - paddleHeight;
        paddle = new Paddle(paddleX, paddleY, paddleWidth, paddleHeight);
        float ballWidth = displayWidth / 20.5f;
        int numBalls = ((players - 1) / 3) + 1;
        int startBallNum = playerId % 3 == 0 ? playerId / 3 : -1;
        for (int i = 0; i < numBalls; i++) {
            Ball ball = new Ball(displayWidth / 2, -1 * ballWidth, ballWidth, ballWidth, 0, 0);
            balls.add(ball);
            if (i == startBallNum) {
                startBall = ball;
            }
        }
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
        } else if (targetPosition + offset > maximumBound) {
            adjustedPosition = maximumBound - offset;
        }
        if (entity instanceof Ball && !xCoordinate && inPaddleDomain(entity) &&
                entity.getY() > displayHeight - entity.getHeight() - paddle.getHeight() &&
                entity.getY() < displayHeight - entity.getHeight()) {
            adjustedPosition = displayHeight - entity.getHeight() - paddle.getHeight();
        }
        return adjustedPosition;
    }

    private boolean onWall(Entity entity) {
        return entity.getX() == 0 || entity.getX() == displayWidth - entity.getWidth();
    }

    private boolean onCeiling(Entity entity) {
        return entity.getY() <= 0;
    }

    private boolean onFloor(Entity entity) {
        return entity.getY() == displayHeight + 100;
    }

    private boolean inPaddleDomain(Entity entity) {
        return paddle.getX() - entity.getWidth() < entity.getX() && entity.getX() < paddle.getX() + paddle.getWidth();
    }

    private boolean onPaddle(Entity entity) {
        return inPaddleDomain(entity) && entity.getY() == displayHeight - entity.getHeight() - paddle.getHeight();
    }

    private int getBallNum(Ball ball) {
        for (int i = 0; i < balls.size(); i++) {
            if (balls.get(i).equals(ball)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an array containing the number of the device the ball is to enter,
     * the horizontal entrance position relative to the new device on a scale from 0 to 1,
     * the rightward velocity scale, and the downward velocity scale.
     */
    private float[] getDeviceEntranceInfo(Ball ball) {
        int device = -1;
        float absoluteX = Float.NaN;
        float dy = -1 * ball.getDownwardVelocity();
        float dx = ball.getRightwardVelocity();
        if (dx == 0) {
            dx = .001f;
        }
        double a = angleBetweenDevices();
        List<Integer> deviceCandidates = new ArrayList<Integer>(2);
        List<Float> xValues = new ArrayList<Float>(2);
        for (int c = 0; c < players && deviceCandidates.size() < 2; c++) {
            float xc = getDeviceEndpoint1(c).x;
            float yc = getDeviceEndpoint1(c).y;
            float x = (float) (((((dy*ball.getX())/dx)-Math.tan(rad(c*(180 - a)))*xc)+yc)/((dy/dx)-Math.tan(rad(c*(180-a)))));
            System.out.println("Device: " + c + ", x: " + x);
            float xc2 = getDeviceEndpoint2(c).x;
            if ((xc < x && x <= xc2) || (xc2 < x && x <= xc)) {
                deviceCandidates.add(c);
                xValues.add(x);
            }
        }
        if (deviceCandidates.size() == 1) {
            device = deviceCandidates.get(0);
        } else {
            for (int i = 0; i < deviceCandidates.size(); i++) {
                float y = (dy / dx) * (xValues.get(i) - ball.getX());
                int c = deviceCandidates.get(i);
                float xc = getDeviceEndpoint1(c).x;
                float yc = getDeviceEndpoint1(c).y;
                float yc2 = deviceEdgeEquation(c, (float)Math.cos(rad(c*(180-a)))*displayWidth+xc);
                if ((yc < y && y <= yc2) || yc2 < y && y <= yc) {
                    device = c;
                    absoluteX = xValues.get(i);
                    break;
                }
            }
        }
        float xc = getDeviceEndpoint1(device).x;
        float relativeX = (float) ((absoluteX - xc) / (Math.cos(rad(device * (180 - a))))) / displayWidth;
        float dxScale = (float)((Math.sin(rad(device * (180 - a)))*dy)+(Math.cos(rad(device * (180-a)))*dx)) / ball.standardVelocity;
        float dyScale = (float)((Math.cos(rad(device * (180 - a)))*dy)-(Math.sin(rad(device * (180-a)))*dx)) / ball.standardVelocity * -1;
        device = (device + playerId + 1) % players;
        return new float[]{device, relativeX, dxScale, dyScale};
    }

    private float deviceEdgeEquation(int device, float x) {
        double angle = angleBetweenDevices();
        PointF endpoint1 = getDeviceEndpoint1(device);
        return (float) Math.tan(rad(device * (180 - angle))) * (x - endpoint1.x) + endpoint1.y;
    }

    private PointF getDeviceEndpoint1(int device) {
        if (device == 0) {
            return new PointF(0, 0);
        }
        return getDeviceEndpoint2(device - 1);
    }

    private PointF getDeviceEndpoint2(int device) {
        double angle = angleBetweenDevices();
        PointF endpoint1 = getDeviceEndpoint1(device);
        float x = (float) ((Math.cos(rad(device * (180 - angle))) * displayWidth) + endpoint1.x);
        float y = deviceEdgeEquation(device, x);
        return new PointF(x, y);
    }

    private double angleBetweenDevices() {
        return (((players - 2) * 180) / players);
    }

    private double rad(double degrees) {
        return degrees * (Math.PI/180);
    }

    private float rand(double min, double max) {
        Random r = new Random();
        return (float) (min + (max - min) * r.nextDouble());
    }
}