package evo.search.ga;

import evo.search.Experiment;
import io.jenetics.Gene;
import io.jenetics.util.RandomRegistry;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DiscreteGene implements Gene<DiscretePoint, DiscreteGene> {

    private DiscretePoint data;

    @Override
    public DiscretePoint getAllele() {
        return data;
    }

    @Override
    public DiscreteGene newInstance() {
        int positions = Experiment.getInstance().getPositions();
        List<Double> distances = Experiment.getInstance().getDistances();
        int position = RandomRegistry.random().nextInt(positions);
        int index = RandomRegistry.random().nextInt(distances.size());
        return new DiscreteGene(new DiscretePoint(position, distances.get(index)));
    }

    @Override
    public DiscreteGene newInstance(DiscretePoint value) {
        return of(value);
    }

    public static DiscreteGene of(DiscretePoint value) {
        return new DiscreteGene(value.clone());
    }

    public static DiscreteGene of(double distance) {
        int availablePositions = Experiment.getInstance().getPositions();
        int position = RandomRegistry.random().nextInt(availablePositions);
        return new DiscreteGene(new DiscretePoint(position, distance));
    }

    @Override
    public boolean isValid() {
        boolean distanceValid = Experiment.getInstance().getDistances().contains(data.getDistance());
        boolean positionValid = Experiment.getInstance().getPositions() >= data.getPosition();
        return distanceValid && positionValid;
    }
}
