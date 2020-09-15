package evo.search.ga;

import evo.search.io.entities.Configuration;
import evo.search.io.entities.XmlEntity;
import evo.search.io.service.XmlService;
import io.jenetics.AbstractChromosome;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Chromosome consisting of a permutations of the distances
 * from the {@link Configuration} in an evolved order
 * associated with valid positional indices.
 */
public class DiscreteChromosome extends AbstractChromosome<DiscreteGene> implements XmlEntity<DiscreteChromosome> {


    /**
     * Create a new {@code DiscreteChromosome} from the given {@code genes}
     * array.
     *
     * @param genes the genes that form the chromosome.
     * @throws NullPointerException     if the given gene array is {@code null}.
     * @throws IllegalArgumentException if the length of the gene sequence is
     *                                  empty.
     */
    public DiscreteChromosome(final ISeq<? extends DiscreteGene> genes) {
        super(genes);
    }

    /**
     * Create a new {@code DiscreteChromosome} from the given {@code genes}
     * array.
     *
     * @param genes the genes that form the chromosome.
     * @throws NullPointerException     if the given gene array is {@code null}.
     * @throws IllegalArgumentException if the length of the gene sequence is
     *                                  empty.
     */
    public DiscreteChromosome(final DiscreteGene... genes) {
        super(ISeq.of(genes));
    }

    @Override
    public Chromosome<DiscreteGene> newInstance(final ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.map(DiscreteGene::clone));
    }

    @Override
    public Chromosome<DiscreteGene> newInstance() {
        return new DiscreteChromosome(_genes.map(DiscreteGene::newInstance));
    }

    @Override
    public Element serialize() {
        final Element root = new DefaultElement("discreteChromosome");
        if (_genes.size() > 0)
            root.addAttribute("positions", String.valueOf(gene().getPositions()));
        for (final DiscreteGene gene : _genes)
            root.add(gene.serialize());
        return root;
    }

    @Override
    public DiscreteChromosome parse(final Element element) {
        final Attribute positionsAttribute = element.attribute("positions");
        final short positions = positionsAttribute == null
                ? 3
                : Short.parseShort(positionsAttribute.getValue());

        final List<DiscreteGene> genes = new ArrayList<>();
        XmlService.forEach("gene", element, geneElement -> {
            genes.add(new DiscreteGene(positions, 0, 0).parse(geneElement));
        });
        return new DiscreteChromosome(genes.toArray(DiscreteGene[]::new));
    }
}
