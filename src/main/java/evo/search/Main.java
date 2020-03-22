package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.view.Canvas;
import evo.search.view.MainView;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Line2D;

@Slf4j
public class Main {

    public static void main(String[] args) {
        int length = 15 + RandomRegistry.random().nextInt(30);
        Experiment.init(length, 2);


        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                Experiment::fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );

        Genotype<DiscreteGene> result = Engine.builder(problem).build().stream().limit(100).collect(EvolutionResult.toBestGenotype());


        MainView mainView = new MainView();

        mainView.getCanvas().render(Experiment.getInstance().getTreasure().toPoint2D(), Canvas.PointStyle.CROSS);
        result.chromosome().forEach(gene -> mainView.getCanvas().render(gene.getAllele().toPoint2D()));
        int available = Experiment.getInstance().getPositions();

        for (int position = 0; position < available; position++) {
            int x = (int) Math.cos(position / (double) available * 2 * Math.PI) * 100;
            int y = (int) Math.sin(position / (double) available * 2 * Math.PI) * 100;
            mainView.getCanvas().render(new Line2D.Double(0, 0, x, y));
        }

    }
}
