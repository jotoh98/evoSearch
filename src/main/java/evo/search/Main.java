package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.view.MainView;
import evo.search.view.Style;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.UniformCrossover;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

@Slf4j
public class Main {

    public static void main(String[] args) {
        int length = 15 + RandomRegistry.random().nextInt(30);
        Experiment.init(length, 3);


        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                Experiment::fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );


        Genotype<DiscreteGene> result = Engine
                .builder(problem)
                .alterers(
                        new UniformCrossover<>(0.2),
                        new Mutator<>(0.15)
                )
                .build()
                .stream()
                .limit(10000)
                .collect(EvolutionResult.toBestGenotype());


        MainView mainView = new MainView();

        Experiment.getInstance().getTreasures().forEach(
                treasure -> mainView.getCanvas().enqueue(
                        treasure.toPoint2D(),
                        Style.builder().shape(Style.Shape.CROSS).build()
                )
        );
        result.chromosome().forEach(gene -> mainView.getCanvas().enqueue(gene.getAllele().toPoint2D()));
        int availablePosition = Experiment.getInstance().getPositions();

        for (int position = 0; position < availablePosition; position++) {
            int x = (int) Math.round(Math.cos(position / (double) availablePosition * 2 * Math.PI) * 100);
            int y = (int) Math.round(Math.sin(position / (double) availablePosition * 2 * Math.PI) * 100);
            mainView.getCanvas().enqueue(
                    new Line2D.Double(0, 0, x, y),
                    Style.builder()
                            .color(Color.lightGray)
                            .stroke(new BasicStroke(.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1}, 0))
                            .build()
            );
        }

        if (availablePosition > 2) {
            Point2D previous = new Point2D.Double();
            for (final DiscreteGene discreteGene : result.chromosome()) {
                Point2D current = discreteGene.getAllele().toPoint2D();
                mainView.getCanvas().enqueue(new Line2D.Double(previous, current));
                previous = current;
            }
        }

    }
}
