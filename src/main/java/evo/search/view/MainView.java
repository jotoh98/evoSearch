package evo.search.view;

import evo.search.Experiment;
import evo.search.ga.DiscreteGene;
import io.jenetics.Genotype;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.concurrent.CompletableFuture;

@Getter
public class MainView extends JFrame {

    private Canvas canvas = new Canvas();

    private JPanel upperContainer = new JPanel();

    private JPanel configurationContainer = new JPanel();

    private JTextField limit = new JTextField("1000");

    public MainView() {
        setSize(500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setupConfiguration();
        setupMenu();
        add(upperContainer, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        setVisible(true);
    }

    private void setupMenu() {
        upperContainer.setLayout(new BoxLayout(upperContainer, BoxLayout.Y_AXIS));
        Button editConfiguration = new Button("Edit configuration");
        editConfiguration.addActionListener(
                e -> configurationContainer.setVisible(!configurationContainer.isVisible())
        );
        upperContainer.add(editConfiguration);
        upperContainer.add(configurationContainer);
        Button run = new Button("Run");
        run.addActionListener(e -> {
            run.setEnabled(true);
            CompletableFuture.runAsync(() -> {
                Genotype<DiscreteGene> evolve = Experiment.evolve(10, 1000);
                canvas.clear();
                renderIndividual(evolve);
            }).thenRun(() -> {
                run.setEnabled(true);
            });
        });
        upperContainer.add(run);
    }

    private void setupConfiguration() {
        configurationContainer.setVisible(false);
        configurationContainer.setLayout(new GridLayout(0, 2, 5, 5));
        configurationContainer.add(new JLabel("Iterations:"));
        configurationContainer.add(limit);
    }

    private void renderIndividual(final Genotype<DiscreteGene> result) {
        Experiment.getInstance().getTreasures().forEach(
                treasure -> canvas.enqueue(
                        treasure.toPoint2D(),
                        Style.builder().shape(Style.Shape.CROSS).build()
                )
        );
        result.chromosome().forEach(gene -> canvas.enqueue(gene.getAllele().toPoint2D()));
        int availablePosition = Experiment.getInstance().getPositions();

        for (int position = 0; position < availablePosition; position++) {
            int x = (int) Math.round(Math.cos(position / (double) availablePosition * 2 * Math.PI) * 100);
            int y = (int) Math.round(Math.sin(position / (double) availablePosition * 2 * Math.PI) * 100);
            canvas.enqueue(
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
                canvas.enqueue(new Line2D.Double(previous, current));
                previous = current;
            }
        }
    }
}
