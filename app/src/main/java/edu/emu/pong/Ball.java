package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Ball extends Entity {

    private int id;

    public Ball(float x, float y, float width, float height, float rightwardVelocityScale, float downwardVelocityScale, int id) {
        super(x, y, width, height, width / 3.33f, rightwardVelocityScale, downwardVelocityScale);
        this.id = id;
    }

    protected void draw(Canvas canvas, Paint paint) {
        if (visible) {
            canvas.drawArc(new RectF(getX(), getY(), getX() + width, getY() + height), 0, 360, true, paint);
        }
    }

    public int getId() {
        return id;
    }
}