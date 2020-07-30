package evo.search.ga;

import evo.search.util.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.geom.Point2D;

/**
 * Polar coordinate with discretized angle.
 */
@AllArgsConstructor
@Data
public class DiscretePoint implements Cloneable {

    /**
     * Amount of all available rays where a discrete point can be placed.
     * Determines the relative position for {@link #getAngle()}.
     */
    int positions;

    /**
     * Index of the angle used for the corresponding polar coordinate.
     */
    int position;

    /**
     * Distance of the polar coordinate.
     */
    double distance;

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
        return position / ((double) positions) * 2 * Math.PI;
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
            return new DiscretePoint(positions, position, distance);
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
        return MathUtils.distance(getAngle(), distance, other.getAngle(), other.distance);
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
     * @param other Other {@link DiscretePoint} to swap the distance with.
     */
    public void swapDistances(DiscretePoint other) {
        final double temp = this.distance;
        distance = other.distance;
        other.distance = temp;
    }

    /**
     * Swap the position with another {@link DiscretePoint}.
     *
     * @param other Other {@link DiscretePoint} to swap the position with.
     */
    public void swapPositions(DiscretePoint other) {
        final int temp = position;
        position = other.position;
        other.position = temp;
    }
}
