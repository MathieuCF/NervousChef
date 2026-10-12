package nervouschef;

import java.awt.Point;

/**
 *
 * @author 20254337
 */
public class Vector2D {
    public final int x;
    public final int y;

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Static helper to create a vector between two points (B - A)
    public static Vector2D between(Point a, Point b) {
        return new Vector2D(b.x - a.x, b.y - a.y);
    }

    public double getMagnitude() {  // gets vector length
        return Math.sqrt((double) x * x + (double) y * y);
    }

    public double dot(Vector2D other) {  // gets dot product between this vector and another one
        return (double) this.x * other.x + (double) this.y * other.y;
    }

    /**
     * Finds the shortest distance from a standalone Point P 
     * to the line segment defined by Point A and Point B.
     */
    public static double shortestDistanceToSegment(Point a, Point b, Point p) {
        Vector2D ab = Vector2D.between(a, b);
        Vector2D ap = Vector2D.between(a, p);

        double abLenSq = (double) ab.x * ab.x + (double) ab.y * ab.y;

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
