package evo.search.io.entities;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.io.service.XmlService;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionDurations;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor
@Data
public class Run implements XmlEntity {

    private static final List<String> DURATION_NAMES = Arrays.asList(
            "offspringSelection", "survivorsSelection", "offspringAlter",
            "offspringFilter", "survivorFilter", "evaluation", "evolve"
    );

    private List<EvolutionResult<DiscreteGene, Double>> results = new ArrayList<>();

    private List<Phenotype<DiscreteGene, Double>> individuals = new ArrayList<>();

    private static Element serialize(final ISeq<Phenotype<DiscreteGene, Double>> population) {
        return XmlService.appendElementList("population", population, Run::serialize);
    }

    private static Element serialize(final Phenotype<DiscreteGene, Double> phenotype) {
        return XmlService.appendElementList("individual", phenotype.genotype().chromosome(), gene ->
                XmlService.writePoint("point", gene.getAllele())
        ).addAttribute("fitness", String.valueOf(phenotype.fitness()));
    }

    private static Element serialize(final EvolutionDurations durations) {
        return XmlService.appendElementList("durations", DURATION_NAMES, name -> {
            try {
                Method declaredMethod = EvolutionDurations.class.getDeclaredMethod(name + "Duration");
                Duration duration = (Duration) declaredMethod.invoke(durations);
                Element element = DocumentHelper.createElement(name);
                element.addAttribute("value", String.valueOf(duration.getNano()));
                return element;
            } catch (Exception ignored) {
            }
            return null;
        });
    }

    private static EvolutionResult<DiscreteGene, Double> parseResult(Optimize optimize, int total, int generation, Element element) {
        final Element populationElement = element.element("population");
        final Element durationsElement = element.element("durations");

        ISeq<Phenotype<DiscreteGene, Double>> population = parsePopulation(generation, populationElement);

        Duration[] durations = DURATION_NAMES.stream().map(durationsElement::element)
                .filter(Objects::nonNull)
                .map(durationElement -> durationElement.attribute("value"))
                .filter(Objects::nonNull)
                .map(Attribute::getValue)
                .map(Long::parseLong)
                .map(Duration::ofNanos)
                .toArray(Duration[]::new);

        EvolutionDurations evolutionDurations = EvolutionDurations.ZERO;
        if (durations.length > 6) {
            evolutionDurations = EvolutionDurations.of(
                    durations[0],
                    durations[1],
                    durations[2],
                    durations[3],
                    durations[4],
                    durations[5],
                    durations[6]);
        }

        final AtomicInteger killCount = new AtomicInteger();
        final AtomicInteger invalidCount = new AtomicInteger();
        final AtomicInteger alterCount = new AtomicInteger();
        XmlService.readProperties(element, (name, value) -> {
            switch (name) {
                case "killCount":
                    killCount.set(Integer.parseInt(value));
                    break;
                case "invalidCount":
                    invalidCount.set(Integer.parseInt(value));
                    break;
                case "alterCount":
                    alterCount.set(Integer.parseInt(value));
            }
        });

        return EvolutionResult.of(optimize, population, generation, total, evolutionDurations, killCount.get(), invalidCount.get(), alterCount.get());
    }

    private static ISeq<Phenotype<DiscreteGene, Double>> parsePopulation(final int generation, final Element populationElement) {
        ArrayList<Phenotype<DiscreteGene, Double>> discretePhenotypes = new ArrayList<>();
        XmlService.forEach("individual", populationElement, individualElement -> {
            Phenotype<DiscreteGene, Double> discretePhenotype = parsePhenotype(generation, individualElement);
            if (discretePhenotype == null) return;
            discretePhenotypes.add(discretePhenotype);
        });

        Iterator<Phenotype<DiscreteGene, Double>> iterator = discretePhenotypes.iterator();

        return ISeq.of(iterator::next, discretePhenotypes.size());
    }

    @Nullable
    private static Phenotype<DiscreteGene, Double> parsePhenotype(final int generation, final Element individualElement) {
        final ArrayList<DiscreteGene> discreteGenes = new ArrayList<>();
        XmlService.forEach("point", individualElement, pointElement -> {
            DiscretePoint discretePoint = XmlService.readPoint(pointElement);
            if (discretePoint != null) {
                discreteGenes.add(DiscreteGene.of(discretePoint));
            }
        });

        Attribute fitnessAttribute = individualElement.attribute("fitness");

        if (fitnessAttribute == null) {
            return null;
        }

        double fitness = Double.parseDouble(fitnessAttribute.getValue());

        return Phenotype.of(Genotype.of(
                DiscreteChromosome.of(
                        ISeq.of(discreteGenes.toArray(DiscreteGene[]::new)))
                ),
                generation,
                fitness
        );
    }

    public static Run parseRun(final Element element) {
        Run run = new Run();
        run.parse(element);
        return run;
    }

    public void addResult(EvolutionResult<DiscreteGene, Double> result) {
        results.add(result);
    }

    public void addIndividual(Phenotype<DiscreteGene, Double> individual) {
        individuals.add(individual);
    }

    @Override
    public Element serialize() {
        final Element rootElement = DocumentHelper.createElement("run");
        final Element propertiesElement = rootElement.addElement("properties");
        final Element historyElement = rootElement.addElement("history");

        if (results.size() == 0) {
            return rootElement;
        }

        final String optimizerName = results.get(0)
                .optimize()
                .name();

        propertiesElement.add(XmlService.writeProperty("optimizer", optimizerName));

        results.forEach(result -> historyElement.add(serialize(result)));

        return rootElement;
    }

    private Element serialize(EvolutionResult<DiscreteGene, Double> result) {
        final Element resultElement = DocumentHelper.createElement("result");
        resultElement.add(serialize(result.population()));
        resultElement.add(serialize(result.durations()));

        Element killCountElement = XmlService.writeProperty("killCount", result.killCount());
        Element invalidCountElement = XmlService.writeProperty("invalidCount", result.invalidCount());
        Element alterCountElement = XmlService.writeProperty("alterCount", result.alterCount());

        resultElement.add(killCountElement);
        resultElement.add(invalidCountElement);
        resultElement.add(alterCountElement);
        return resultElement;
    }

    @Override
    public void parse(final Element element) {
        final Element propertiesElement = element.element("properties");
        final Element historyElement = element.element("history");

        if (propertiesElement == null || historyElement == null) {
            return;
        }

        final AtomicReference<Optimize> optimize = new AtomicReference<>(null);
        XmlService.readProperties(propertiesElement, (name, value) -> {
            if (name.equals("optimizer")) {
                optimize.set(Optimize.valueOf(value));
            }
        });

        if (optimize.get() == null) {
            return;
        }

        final int total = historyElement.elements("result").size();
        final AtomicInteger generation = new AtomicInteger();
        XmlService.forEach("result", historyElement, resultElement ->
                results.add(parseResult(optimize.get(), total, generation.getAndIncrement(), resultElement))
        );
    }
}
