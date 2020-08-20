package evo.search.io.entities;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.service.XmlService;
import io.jenetics.AbstractAlterer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Environment configuration for the evolution.
 *
 * @see Evolution#run()
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Configuration implements Cloneable, XmlEntity<Configuration> {

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
     * The amount of positions available for the {@link DiscretePoint}s.
     */
    @Builder.Default
    private int positions = 3;
    /**
     * Input distances to choose a permutation from.
     * Forms single {@link DiscreteChromosome}s.
     */
    @Builder.Default
    private List<Double> distances = new ArrayList<>();
    /**
     * List of treasure {@link DiscretePoint}s to search for.
     */
    @Builder.Default
    private List<DiscretePoint> treasures = new ArrayList<>();

    @Builder.Default
    private Evolution.Fitness fitness = Evolution.Fitness.getDefault();

    @Builder.Default
    private List<? extends DiscreteAlterer> alterers = new ArrayList<>(
            Arrays.asList(
                    new SwapGeneMutator(0.5),
                    new SwapPositionsMutator(0.5)
            )
    );

    @Builder.Default
    private int offspring = 15;

    @Builder.Default
    private int survivors = 10;

    @Builder.Default
    private int population = 20;

    @Builder.Default
    private boolean chooseWithoutPermutation = true;

    @Builder.Default
    private double distanceMutationDelta = 1.0;

    @Override
    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException("Configuration could not be cloned", e);
        }
    }

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

    private static DiscretePoint parseTreasure(final Element element) {
        final Attribute amountPositions = element.attribute("amount");
        final Attribute positionAttribute = element.attribute("position");
        final Attribute distanceAttribute = element.attribute("distance");
        if (positionAttribute == null || distanceAttribute == null || amountPositions == null) {
            return null;
        }
        try {
            return new DiscretePoint(
                    Integer.parseInt(amountPositions.getValue()),
                    Integer.parseInt(positionAttribute.getValue()),
                    Double.parseDouble(distanceAttribute.getValue())
            );
        } catch (final NumberFormatException | NullPointerException ignored) {
            return null;
        }
    }

    private static Element writeTreasure(final DiscretePoint point) {
        return new DefaultElement("treasure")
                .addAttribute("amount", String.valueOf(point.getPositions()))
                .addAttribute("position", String.valueOf(point.getPosition()))
                .addAttribute("distance", String.valueOf(point.getDistance()));
    }

    private static Element writeAlterer(final DiscreteAlterer alterer) {
        double probability = .5;
        if (alterer instanceof AbstractAlterer) {
            probability = ((AbstractAlterer<?, ?>) alterer).probability();
        }
        return new DefaultElement("alterer")
                .addAttribute("method", alterer.getClass().getName())
                .addAttribute("probability", Double.toString(probability));
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
                case "survivors":
                    setSurvivors(Integer.parseInt(value));
                    break;
                case "population":
                    setPopulation(Integer.parseInt(value));
                    break;
                case "fitness":
                    Evolution.Fitness fitness;
                    try {
                        fitness = Evolution.Fitness.valueOf(value);
                    } catch (final IllegalArgumentException ignored) {
                        fitness = Evolution.Fitness.getDefault();
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
            final ArrayList<DiscretePoint> treasures = new ArrayList<>();
            XmlService.forEach("treasure", treasuresElement, element -> {
                final DiscretePoint discretePoint = parseTreasure(element);
                if (discretePoint != null) {
                    treasures.add(discretePoint);
                }
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
                XmlService.writeProperty("survivors", getSurvivors()),
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

}
