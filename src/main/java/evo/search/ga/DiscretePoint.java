package evo.search.ga;

import evo.search.Configuration;
import evo.search.Environment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.geom.Point2D;

/**
 * Polar coordinate with discretized angle.
 */
@AllArgsConstructor
@Getter
public class DiscretePoint implements Cloneable {

    /**
     * Index of the angle used for the corresponding polar coordinate.
     * Is used with {@link Configuration#getPositions()}.
     */
    private int position;

    /**
     * Distance of the polar coordinate.
     */
    private double distance;

    /**
     * Transfer the discretized polar coordinate to a cartesian coordinate.
     *
     * @return Cartesian coordinate point.
     */
    public Point2D toPoint2D() {
        double angle = getAngle();
        return new Point2D.Double(distance * Math.cos(angle), distance * Math.sin(angle));
    }

    /**
     * Get the radian angle for the discretized angle.
     *
     * @return Radian angle of the polar coordinate.
     */
    public double getAngle() {
        double positions = Environment.getInstance().getConfiguration().getPositions();
        return position / positions * 2 * Math.PI;
    }

    /**
     * Clones the {@link DiscretePoint}.
     *
     * @return A new instance of the {@link DiscretePoint}.
     */
    @Override
    public DiscretePoint clone() {
        try {
            return (DiscretePoint) super.clone();
        } catch (CloneNotSupportedException ignore) {
            return new DiscretePoint(position, distance);
        }
    }

    /**
     * Returns a string representation of the discrete point.
     *
     * @return A string representation of the discrete point.
     */
    @Override
    public String toString() {
        return "DiscretePoint(" + position + ", " + distance + ")";
    }

    /**
     * Calculate the euclidean distance to another {@link DiscretePoint}.
     *
     * @param other Other discrete point.
     * @return The euclidean distance to another {@link DiscretePoint}.
     */
    public double distance(DiscretePoint other) {
        double subtract = 2 * distance * other.getDistance() * Math.cos(getAngle() - other.getAngle());
        return Math.sqrt(getDistanceSquared() + other.getDistanceSquared() - subtract);
    }

    /**
     * Get the polar coordinates distance squared.
     *
     * @return The polar coordinates distance squared.
     */
    public double getDistanceSquared() {
        return distance * distance;
    }

    /**
     * Swap the distance with another {@link DiscretePoint}.
     *
     * @param other Other {@link DiscretePoint} to swap distances with.
     */
    public void swapDistance(DiscretePoint other) {
        final int temp = position;
        position = other.position;
        other.position = temp;
    }
}
