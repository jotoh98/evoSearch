package evo.search.io.entities;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.DistanceMutator;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.service.XmlService;
import evo.search.util.ListUtils;
import io.jenetics.AbstractAlterer;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Environment configuration for the evolution.
 *
 * @see Evolution#run()
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Configuration implements Cloneable, XmlEntity<Configuration>, Serializable {

    /**
     * Version for configuration compatibility checks.
     */
    @Builder.Default
    private String version = Main.VERSION;
    /**
     * The configurations name.
     */
    @Builder.Default
    private String name = "Unnamed";
    /**
     * Last execution limit for the evolution method.
     */
    @Builder.Default
    private int limit = 1000;
    /**
     * The amount of positions available for the {@link DiscreteGene}s.
     */
    @Builder.Default
    private int positions = 3;
    /**
     * Input distances to choose a permutation from.
     * Forms single {@link DiscreteGene} chromosomes.
     */
    @Builder.Default
    private List<Double> distances = new ArrayList<>();
    /**
     * List of treasure {@link DiscreteGene}s to search for.
     */
    @Builder.Default
    private List<DiscreteGene> treasures = new ArrayList<>();

    /**
     * Fitness method used to evaluate the individuals.
     *
     * @see Evolution.Fitness
     */
    @Builder.Default
    private Evolution.Fitness fitness = Evolution.Fitness.MAX_AREA;

    /**
     * List of alterers used during the {@link Evolution}'s mutation phase.
     */
    @Builder.Default
    private transient List<? extends DiscreteAlterer> alterers = new ArrayList<>(
            Arrays.asList(
                    new SwapGeneMutator(0.5),
                    new SwapPositionsMutator(0.5)
            )
    );

    /**
     * Amount of offspring individuals.
     * Has to be less or equal than {@link #population}.
     */
    @Builder.Default
    private int offspring = 15;

    /**
     * Population size.
     */
    @Builder.Default
    private int population = 20;

    /**
     * State of whether to choose the distances in a permutation
     * during the shuffle of new individuals.
     *
     * @see DiscreteGene#newInstance()
     */
    @Builder.Default
    private boolean chooseWithoutPermutation = true;

    /**
     * Value of maximum distance change during the mutation.
     *
     * @see evo.search.ga.mutators.DistanceMutator
     */
    @Builder.Default
    private double distanceMutationDelta = 1.0;

    /**
     * Parse a {@link DiscreteAlterer} from an {@link Element}.
     *
     * @param element element containing the discrete alterer
     * @return parsed discrete alterer
     * @see #parse(Document)
     */
    private static DiscreteAlterer parseAlterer(final Element element) {
        final Attribute methodAttribute = element.attribute("method");
        final Attribute probabilityAttribute = element.attribute("probability");
        if (methodAttribute == null) {
            return null;
        }
        double probability = .5;

        if (probabilityAttribute != null) {
            try {
                probability = Double.parseDouble(probabilityAttribute.getValue());
            } catch (final NumberFormatException ignored) {
            }
        }

        try {
            return (DiscreteAlterer) Class
                    .forName(methodAttribute.getValue())
                    .getConstructor(double.class)
                    .newInstance(probability);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Write a {@link DiscreteGene} to an {@link Element}.
     *
     * @param point treasure point to serialize
     * @return element of serialized treasure point
     * @see #serialize()
     */
    private static Element writeTreasure(final DiscreteGene point) {
        return new DefaultElement("treasure")
                .addAttribute("position", String.valueOf(point.getPosition()))
                .addAttribute("distance", String.valueOf(point.getDistance()));
    }

    /**
     * Write a {@link DiscreteAlterer} to an {@link Element}.
     *
     * @param alterer discrete alterer to serialize
     * @return element of serialized discrete alterer
     * @see #serialize()
     */
    private static Element writeAlterer(final DiscreteAlterer alterer) {
        double probability = .5;
        if (alterer instanceof AbstractAlterer) {
            probability = ((AbstractAlterer<?, ?>) alterer).probability();
        }
        return new DefaultElement("alterer")
                .addAttribute("method", alterer.getClass().getName())
                .addAttribute("probability", Double.toString(probability));
    }

    /**
     * Parse a treasure-{@link DiscreteGene} from an {@link Element}.
     *
     * @param element element containing the discrete alterer
     * @return parsed treasure point
     * @see #parse(Document)
     */
    private DiscreteGene parseTreasure(final Element element) {
        final Attribute positionAttribute = element.attribute("position");
        final Attribute distanceAttribute = element.attribute("distance");
        if (positionAttribute == null || distanceAttribute == null) {
            return null;
        }
        try {
            return new DiscreteGene(
                    positions,
                    Integer.parseInt(positionAttribute.getValue()),
                    Double.parseDouble(distanceAttribute.getValue())
            );
        } catch (final NumberFormatException | NullPointerException ignored) {
            return null;
        }
    }

    @Override
    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException("Configuration could not be cloned", e);
        }
    }

    @Override
    public Configuration parse(final Document document) {
        final Element rootElement = document.getRootElement();

        if (rootElement == null) return this;

        final Element properties = rootElement.element("properties");

        XmlService.readProperties(properties, (name, value) -> {
            switch (name) {
                case "version":
                    setVersion(value);
                    break;
                case "name":
                    setName(value);
                    break;
                case "limit":
                    setLimit(Integer.parseInt(value));
                    break;
                case "positions":
                    setPositions(Integer.parseInt(value));
                    break;
                case "offspring":
                    setOffspring(Integer.parseInt(value));
                    break;
                case "population":
                    setPopulation(Integer.parseInt(value));
                    break;
                case "fitness":
                    Evolution.Fitness fitness;
                    try {
                        fitness = Evolution.Fitness.valueOf(value);
                    } catch (final IllegalArgumentException ignored) {
                        fitness = Evolution.Fitness.MAX_AREA;
                    }
                    setFitness(fitness);
                    break;
                case "noPermutation":
                    setChooseWithoutPermutation(Boolean.parseBoolean(value));
                    break;
                case "distanceDelta":
                    setDistanceMutationDelta(Double.parseDouble(value));
                    break;
            }
        });


        final Element treasuresElement = rootElement.element("treasures");
        if (treasuresElement != null) {
            final ArrayList<DiscreteGene> treasures = new ArrayList<>();
            XmlService.forEach("treasure", treasuresElement, element -> {
                final DiscreteGene gene = parseTreasure(element);
                if (gene != null)
                    treasures.add(gene);
            });
            setTreasures(treasures);
        }


        final Element distancesElement = rootElement.element("distances");
        if (distancesElement != null) {
            final ArrayList<Double> distances = new ArrayList<>();
            XmlService.forEach("distance", distancesElement, element -> {
                try {
                    final double distance = Double.parseDouble(element.getText());
                    distances.add(distance);
                } catch (final NumberFormatException ignored) {
                }
            });
            setDistances(distances);
        }

        final Element alterersElement = rootElement.element("alterers");
        if (alterersElement != null) {
            final ArrayList<DiscreteAlterer> alterers = new ArrayList<>();
            XmlService.forEach("alterer", alterersElement, element -> {
                try {
                    final DiscreteAlterer alterer = parseAlterer(element);
                    if (alterer != null) {
                        alterers.add(alterer);
                    }
                } catch (final NumberFormatException ignored) {
                }
            });
            setAlterers(alterers);
        }

        return this;
    }

    @Override
    public Document serialize() {
        final Element root = new DefaultElement("configuration");
        final Element propertiesElement = root.addElement("properties");

        Arrays.asList(
                XmlService.writeProperty("name", getName()),
                XmlService.writeProperty("version", getVersion()),
                XmlService.writeProperty("limit", getLimit()),
                XmlService.writeProperty("positions", getPositions()),
                XmlService.writeProperty("offspring", getOffspring()),
                XmlService.writeProperty("population", getPopulation()),
                XmlService.writeProperty("fitness", getFitness().name()),
                XmlService.writeProperty("noPermutation", isChooseWithoutPermutation()),
                XmlService.writeProperty("distanceDelta", getDistanceMutationDelta())

        )
                .forEach(propertiesElement::add);

        final Element treasuresElement = root.addElement("treasures");
        XmlService.appendElementList(treasuresElement, getTreasures(), Configuration::writeTreasure);

        final Element distancesElement = root.addElement("distances");
        XmlService.appendElementList(distancesElement, getDistances(), aDouble -> XmlService.simpleElement("distance", aDouble));

        final Element alterersElement = root.addElement("alterers");
        XmlService.appendElementList(alterersElement, getAlterers(), Configuration::writeAlterer);

        return DocumentHelper.createDocument(root);
    }

    /**
     * Shuffle a new chromosome with {@link DiscreteGene}s from the distance list.
     *
     * @return shuffled chromosome from distance list
     */
    public Chromosome<DiscreteGene> shuffle() {
        final List<Double> shuffled;
        if (!chooseWithoutPermutation) {
            shuffled = ListUtils.deepClone(this.distances, Double::doubleValue);
            Collections.shuffle(shuffled);
        } else {
            shuffled = new ArrayList<>();
            for (int i = 0; i < distances.size(); i++)
                shuffled.add(ListUtils.chooseRandom(distances));
        }

        final DiscreteGene[] genes = new DiscreteGene[shuffled.size()];
        for (int i = 0; i < shuffled.size(); i++)
            genes[i] = new DiscreteGene(positions, RandomRegistry.random().nextInt(positions), shuffled.get(i));

        return new DiscreteChromosome(genes);
    }

    /**
     * Validator for a sequence of {@link DiscreteGene}s.
     *
     * @param geneSequence sequence of genes to validate
     * @return whether the gene sequence is valid or not
     */
    public boolean geneSequenceValidator(final ISeq<DiscreteGene> geneSequence) {
        if (chooseWithoutPermutation || (alterers.stream().anyMatch(alterer -> alterer instanceof DistanceMutator) && distanceMutationDelta > 0))
            return true;

        final Set<Double> distanceSet = new HashSet<>();
        for (final DiscreteGene gene : geneSequence)
            distanceSet.add((double) gene.getDistance());
        return distanceSet.containsAll(distances);
    }
}
