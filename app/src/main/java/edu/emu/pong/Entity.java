package edu.emu.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
    private static List<Entity> instances = new ArrayList<Entity>();
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float dx;
    protected float dy;
    protected float standardVelocity;
    private Context context;
    protected float rightwardVelocityScale;
    protected float downwardVelocityScale;

    public Entity(float x, float y, float width, float height, float standardVelocity, float rightwardVelocityScale, float downwardVelocityScale) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.standardVelocity = standardVelocity;
        setRightwardVelocityScale(rightwardVelocityScale);
        setDownwardVelocityScale(downwardVelocityScale);
        instances.add(this);
    }

    protected abstract void draw(Canvas canvas, Paint paint);

    public synchronized float getX() {
        return x;
    }

    public synchronized float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRightwardVelocity() {
        return dx;
    }

    public float getDownwardVelocity() {
        return dy;
    }

    public float getRightwardVelocityScale() {
        return rightwardVelocityScale;
    }

    public float getDownwardVelocityScale() {
        return downwardVelocityScale;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setRightwardVelocityScale(float scale) {
        rightwardVelocityScale = scale;
        dx = standardVelocity * scale;
    }

    public void setDownwardVelocityScale(float scale) {
        downwardVelocityScale = scale;
        dy = standardVelocity * scale;
    }

    public static List<Entity> getInstances() {
        return instances;
    }
}