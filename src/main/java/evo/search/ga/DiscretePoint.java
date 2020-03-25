package evo.search.ga;

import evo.search.Experiment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.geom.Point2D;

@AllArgsConstructor
@Getter
public class DiscretePoint implements Cloneable {
    private int position;
    private double distance;

    public Point2D toPoint2D() {
        double angle = getAngle();
        return new Point2D.Double(distance * Math.cos(angle), distance * Math.sin(angle));
    }

    public double getAngle() {
        double positions = Experiment.getInstance().getPositions();
        return position / positions * 2 * Math.PI;
    }

    @Override
    public DiscretePoint clone() {
        try {
            return (DiscretePoint) super.clone();
        } catch (CloneNotSupportedException ignore) {
            return new DiscretePoint(position, distance);
        }
    }

    @Override
    public String toString() {
        return "DiscretePoint(" + position + ", " + distance + ")";
    }

    public double distance(DiscretePoint other) {
        double subtract = 2 * distance * other.getDistance() * Math.cos(getAngle() - other.getAngle());
        return Math.sqrt(getDistanceSquared() + other.getDistanceSquared() - subtract);
    }

    public double getDistanceSquared() {
        return distance * distance;
    }

    public void swapDistance(DiscretePoint other) {
        final int temp = position;
        position = other.position;
        other.position = temp;
    }
}
