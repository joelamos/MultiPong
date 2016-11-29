package edu.emu.pong;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Paddle extends Entity {

    private boolean leftMovement;
    private boolean rightMovement;

    public Paddle(float x, float y, float width, float height) {
        super(x, y, width, height, width / 10, 0, 0);
    }

    @Override
    protected void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(new RectF(getX(), getY(), getX() + getWidth(), getY() + height), paint);
    }

    public void setLeftMovement(boolean move) {
        if (!rightMovement) {
            setRightwardVelocityScale(move ? -1 : 0);
            leftMovement = move;
        }
    }

    public void setRightMovement(boolean move) {
        if (!leftMovement) {
            setRightwardVelocityScale(move ? 1 : 0);
            rightMovement = move;
        }
    }
}