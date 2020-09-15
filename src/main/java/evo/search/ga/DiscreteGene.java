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

/**
 * Discrete genome carrying a {@link Point2D} allele.
 */
@AllArgsConstructor
@Data
public class DiscreteGene implements Gene<Point2D, DiscreteGene>, Serializable, Cloneable {

    /**
     * Configuration providing context for the gene. Mainly used to calculate the allele.
     *
     * @see #allele()
     * @see DiscreteGene#positions
     */
    private short positions;

    /**
     * The discrete genes position index. This is the index of the ray the gene's allele will be
     * sitting on.
     *
     * @see DiscreteGene#position
     */
    private short position;

    /**
     * The genes distance corresponding the the alleles distance.
     *
     * @see DiscreteGene#distance
     */
    private float distance;

    /**
     * Convenience constructor.
     *
     * @param positions amount of rays
     * @param position  index of ray
     * @param distance  distance from origin
     */
    public DiscreteGene(final int positions, final int position, final double distance) {
        this.positions = (short) positions;
        this.position = (short) position;
        this.distance = (float) distance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D allele() {
        final double angle = MathUtils.sectorAngle(positions) * position;
        return new Point2D.Double(distance * Math.cos(angle), distance * Math.sin(angle));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance() {
        final int p = RandomRegistry.random().nextInt(positions);
        final double d = RandomUtils.inRange(distance - .1, distance + .1);
        return new DiscreteGene(positions, p, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance(final Point2D value) {
        final int position = (int) Math.round(Math.atan2(value.getY(), value.getX()) / MathUtils.TWO_PI * positions);
        return new DiscreteGene(positions, position, value.distance(0, 0));
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
        return MathUtils.polarDistance(positions, position, distance, other.position, other.distance);
    }

    /**
     * Clones the {@link DiscreteGene}.
     *
     * @return A new instance of the {@link DiscreteGene}.
     */
    @Override
    public DiscreteGene clone() {
        return new DiscreteGene(positions, position, distance);
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
