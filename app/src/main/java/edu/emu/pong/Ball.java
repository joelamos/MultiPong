package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Ball extends Entity {

    public Ball(float x, float y, float width, float height, float rightwardVelocityScale, float downwardVelocityScale) {
        super(x, y, width, height, (float) (width / 3.33), rightwardVelocityScale, downwardVelocityScale);
    }

    protected void draw(Canvas canvas, Paint paint) {
        canvas.drawArc(new RectF(getX(), getY(), getX() + width, getY() + height), 0, 360, true, paint);
    }
}