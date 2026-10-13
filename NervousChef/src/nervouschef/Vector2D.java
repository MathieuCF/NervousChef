package nervouschef;

import java.awt.Point;

/**
 *
 * @author 20254337
 */
public class Vector2D {
    public final double x;
    public final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Static helper to create a vector between two points (B - A)
    public static Vector2D between(Point a, Point b) {
        return new Vector2D(b.x - a.x, b.y - a.y);
    }

    public double getMagnitude() {  // gets vector length
        return Math.sqrt(x * x + y * y);
    }

    public double dot(Vector2D other) {  // gets dot product between this vector and another one
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Returns a new normalized (unit length) vector.
     * If the vector's magnitude is 0, it returns a zero vector to avoid division by zero.
     */
    public Vector2D normalize() {
        double mag = getMagnitude();
        if (mag == 0.0) {
            return new Vector2D(0.0, 0.0);
        }
        return new Vector2D(x / mag, y / mag);
    }

    /**
     * Finds the shortest distance from a standalone Point P 
     * to the line segment defined by Point A and Point B.
     */
    public static double shortestDistanceToSegment(Point a, Point b, Point p) {
        Vector2D ab = Vector2D.between(a, b);
        Vector2D ap = Vector2D.between(a, p);

        double abLenSq = ab.x * ab.x + ab.y * ab.y;

        // If A and B are the exact same point, the segment is just a single point.
        if (abLenSq == 0) {
            return ap.getMagnitude();
        }

        // Calculate projection factor t, clamped between 0.0 and 1.0
        double t = ap.dot(ab) / abLenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        // Find the closest point coordinates on the segment
        double closestX = a.x + t * ab.x;
        double closestY = a.y + t * ab.y;

        // Return the distance from P to that closest point
        double dx = p.x - closestX;
        double dy = p.y - closestY;
        return Math.sqrt(dx * dx + dy * dy);
    }
}