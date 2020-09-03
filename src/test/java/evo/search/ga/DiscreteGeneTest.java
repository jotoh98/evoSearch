package evo.search.ga;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jotoh
 */
class DiscreteGeneTest {

    /**
     * Test to check if the alleles produced by the discrete gene is correct.
     */
    @Test
    void correctAlleleTest() {
        final DiscreteGene discreteGene = new DiscreteGene(6, 3, 1);
        final Point2D allele = discreteGene.getAllele();
        assertEquals(-1, allele.getX(), 1e-15);
        assertEquals(0, allele.getY(), 1e-15);
        final DiscreteGene phenotype = discreteGene.newInstance(allele);
        assertEquals(discreteGene.getPosition(), phenotype.getPosition());
        assertEquals(discreteGene.getDistance(), phenotype.getDistance(), 1e-15);
    }

}