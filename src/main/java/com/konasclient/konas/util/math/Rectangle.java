package com.konasclient.konas.util.math;

import net.minecraft.util.math.*;

public class Rectangle {
    public final double minX;
    public final double minY;

    public final double maxX;
    public final double maxY;

    public Rectangle(double x1, double y1, double x2, double y2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
    }

    public Rectangle(Vec2d pos1, Vec2d pos2) {
        this(pos1.x, pos1.y, pos2.x, pos2.y);
    }

    public static Rectangle unitRectangle(Vec2d vec3d) {
        return new Rectangle(vec3d.x, vec3d.y, vec3d.x + 1.0D, vec3d.y + 1.0D);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Rectangle)) {
            return false;
        } else {
            Rectangle rectangle = (Rectangle) o;
            if (Double.compare(rectangle.minX, this.minX) != 0) {
                return false;
            } else if (Double.compare(rectangle.minY, this.minY) != 0) {
                return false;
            } else if (Double.compare(rectangle.maxX, this.maxX) != 0) {
                return false;
            } else return Double.compare(rectangle.maxY, this.maxY) == 0;
        }
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.minX);
        int i = (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.minY);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxX);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxY);
        i = 31 * i + (int)(l ^ l >>> 32);
        return i;
    }

    public Rectangle shrink(double x, double y) {
        double d = this.minX;
        double e = this.minY;
        double g = this.maxX;
        double h = this.maxY;
        if (x < 0.0D) {
            d -= x;
        } else if (x > 0.0D) {
            g -= x;
        }

        if (y < 0.0D) {
            e -= y;
        } else if (y > 0.0D) {
            h -= y;
        }

        return new Rectangle(d, e, g, h);
    }

    public Rectangle stretch(Vec2d scale) {
        return this.stretch(scale.x, scale.y);
    }

    public Rectangle stretch(double x, double y) {
        double d = this.minX;
        double e = this.minY;
        double g = this.maxX;
        double h = this.maxY;
        if (x < 0.0D) {
            d += x;
        } else if (x > 0.0D) {
            g += x;
        }

        if (y < 0.0D) {
            e += y;
        } else if (y > 0.0D) {
            h += y;
        }

        return new Rectangle(d, e, g, h);
    }

    public Rectangle expand(double x, double y) {
        double d = this.minX - x;
        double e = this.minY - y;
        double g = this.maxX + x;
        double h = this.maxY + y;
        return new Rectangle(d, e, g, h);
    }

    public Rectangle expand(double value) {
        return this.expand(value, value);
    }

    public Rectangle intersection(Rectangle rectangle) {
        double d = Math.max(this.minX, rectangle.minX);
        double e = Math.max(this.minY, rectangle.minY);
        double g = Math.min(this.maxX, rectangle.maxX);
        double h = Math.min(this.maxY, rectangle.maxY);
        return new Rectangle(d, e, g, h);
    }

    public Rectangle union(Rectangle rectangle) {
        double d = Math.min(this.minX, rectangle.minX);
        double e = Math.min(this.minY, rectangle.minY);
        double g = Math.max(this.maxX, rectangle.maxX);
        double h = Math.max(this.maxY, rectangle.maxY);
        return new Rectangle(d, e, g, h);
    }

    public Rectangle offset(double x, double y) {
        return new Rectangle(this.minX + x, this.minY + y, this.maxX + x, this.maxY + y);
    }

    public Rectangle offset(Vec2d vec2d) {
        return this.offset(vec2d.x, vec2d.y);
    }

    public boolean intersects(Rectangle rectangle) {
        return this.intersects(rectangle.minX, rectangle.minY, rectangle.maxX, rectangle.maxY);
    }

    public boolean intersects(double minX, double minY, double maxX, double maxY) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY;
    }

    public boolean intersects(Vec2d from, Vec2d to) {
        return this.intersects(Math.min(from.x, to.x), Math.min(from.y, to.y), Math.max(from.x, to.x), Math.max(from.y, to.y));
    }

    public boolean contains(Vec2d vec) {
        return this.contains(vec.x, vec.y);
    }

    public boolean contains(double x, double y) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY;
    }

    public double getAverageSideLength() {
        double d = this.getXLength();
        double e = this.getYLength();
        return (d + e) / 2.0D;
    }

    public double getXLength() {
        return this.maxX - this.minX;
    }

    public double getYLength() {
        return this.maxY - this.minY;
    }

    public Rectangle contract(double value) {
        return this.expand(-value);
    }

    public String toString() {
        return "Rect[" + this.minX + ", " + this.minY + "] -> [" + this.maxX + ", " + this.maxY + "]";
    }

    public boolean isValid() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY);
    }

    public Vec2d getCenter() {
        return new Vec2d(MathHelper.lerp(0.5D, this.minX, this.maxX), MathHelper.lerp(0.5D, this.minY, this.maxY));
    }
}