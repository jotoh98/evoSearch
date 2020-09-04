package evo.search.ga;

import evo.search.io.entities.Configuration;
import evo.search.util.MathUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Gene;
import io.jenetics.util.RandomRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Random;

/**
 * Discrete genome carrying a {@link Point2D} allele.
 */
@AllArgsConstructor
@Data
public class DiscreteGene implements Gene<Point2D, DiscreteGene>, Serializable {

    /**
     * Configuration providing context for the gene. Mainly used to calculate the allele.
     *
     * @see #getAllele()
     * @see DiscreteGene#positions
     */
    private int positions;

    /**
     * The discrete genes position index. This is the index of the ray the gene's allele will be
     * sitting on.
     *
     * @see DiscreteGene#position
     */
    private int position;

    /**
     * The genes distance corresponding the the alleles distance.
     *
     * @see DiscreteGene#distance
     */
    private double distance;

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getAllele() {
        final double angle = getAngle();
        return new Point2D.Double(distance * Math.cos(angle), distance * Math.sin(angle));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance() {
        return new DiscreteGene(positions, RandomUtils.inRange(0, positions), distance + RandomUtils.inRange(-.1, .1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance(final Point2D value) {
        final int position = (int) Math.round(Math.atan2(value.getY(), value.getX()) / (2 * Math.PI) * positions);
        return new DiscreteGene(positions, position, value.distance(0, 0));
    }

    /**
     * Returns a shuffled {@link DiscreteGene}.
     * The point's position is randomly selected in the range [0, {@code positions}) and the distance is chosen in the range [{@code minDistance}, {@code maxDistance}).
     * Utilizes the {@link RandomRegistry} from jenetics for thread-safety.
     *
     * @param positions   upper boundary for the points position
     * @param minDistance Minimal distance for the point (inclusive).
     * @param maxDistance Maximal distance for the point (exclusive).
     * @return shuffled discrete point
     * @see Random#nextDouble()
     */
    public static DiscreteGene shuffle(final int positions, final double minDistance, final double maxDistance) {
        final int p = RandomRegistry.random().nextInt(positions);
        final double d = RandomUtils.inRange(minDistance, maxDistance);
        return new DiscreteGene(positions, p, d);
    }

    @Override
    public String toString() {
        return String.format("DiscreteGene(%d, %d, %s)", positions, position, distance);
    }

    /**
     * Calculate the euclidean distance to another {@link DiscreteGene}.
     *
     * @param other other discrete point
     * @return The euclidean distance to another {@link DiscreteGene}.
     */
    public double distance(final DiscreteGene other) {
        return MathUtils.polarDistance(getAngle(), distance, other.getAngle(), other.distance);
    }

    /**
     * Get the radian angle for the discretized angle.
     *
     * @return radian angle of the polar coordinate
     */
    public double getAngle() {
        return position / ((double) positions) * 2 * Math.PI;
    }

    /**
     * Get the polar coordinates distance squared.
     *
     * @return the polar coordinates distance squared
     */
    public double getDistanceSquared() {
        return distance * distance;
    }

    /**
     * Clones the {@link DiscreteGene}.
     *
     * @return A new instance of the {@link DiscreteGene}.
     */
    @Override
    public DiscreteGene clone() {
        try {
            return (DiscreteGene) super.clone();
        } catch (final CloneNotSupportedException ignore) {
            return new DiscreteGene(positions, position, distance);
        }
    }

    /**
     * Prints a small gene representation of the form (4, 5.9).
     *
     * @return small string representation of the gene
     */
    public String printSmall() {
        return String.format("(%s, %s)", position, distance);
    }

    /**
     * Checks, if a gene is valid. That means, that the allele's distance is
     * one of these in the {@link Configuration} and smaller than the positions property.
     *
     * @return whether the discrete gene is valid
     */
    @Override
    public boolean isValid() {
        return distance > 0 && position >= 0 && position < positions && distance >= 0;
    }
}
