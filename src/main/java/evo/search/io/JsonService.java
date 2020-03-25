package evo.search.io;

import evo.search.Experiment;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.util.ISeq;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JsonService {

    public static JSONObject write(DiscretePoint discretePoint) {
        return new JSONObject()
                .accumulate("p", discretePoint.getPosition())
                .accumulate("d", discretePoint.getDistance());
    }

    public static JSONObject write(DiscreteGene discreteGene) {
        return write(discreteGene.getAllele());
    }

    public static JSONObject write(DiscreteChromosome discreteChromosome) {
        JSONObject jsonObject = new JSONObject();
        discreteChromosome.forEach(discreteGene -> jsonObject.append("c", write(discreteGene)));
        return jsonObject;
    }

    public static JSONObject write(Experiment experiment) {
        JSONObject jsonObject = new JSONObject()
                .accumulate("a", experiment.getPositions())
                .accumulate("d", experiment.getDistances());

        if (experiment.getTreasures().size() == 0) {
            jsonObject.put("t", new ArrayList<>());
        }
        if (experiment.getIndividuals().size() == 0) {
            jsonObject.put("i", new ArrayList<>());
        }

        experiment.getTreasures().forEach(discretePoint -> jsonObject.append("t", write(discretePoint)));
        experiment.getIndividuals().forEach(discreteChromosome -> jsonObject.append("i", write(discreteChromosome)));
        return jsonObject;
    }

    public static DiscretePoint readDiscretePoint(JSONObject jsonObject) {
        if (valid(jsonObject, "p", "d")) {
            return new DiscretePoint(jsonObject.getInt("p"), jsonObject.getDouble("d"));
        }
        return null;
    }

    public static DiscreteGene readDiscreteGene(JSONObject jsonObject) {
        DiscretePoint discretePoint = readDiscretePoint(jsonObject);
        if (discretePoint == null) {
            return null;
        }
        return new DiscreteGene(discretePoint);
    }

    public static DiscreteChromosome readDiscreteChromosome(JSONObject jsonObject) {
        if (!valid(jsonObject, "c")) {
            return null;
        }
        List<DiscreteGene> genes = new ArrayList<>();
        jsonObject.getJSONArray("c").forEach(
                o -> genes.add(readDiscreteGene((JSONObject) o))
        );
        DiscreteGene[] discreteGenes = genes.stream()
                .filter(Objects::nonNull)
                .toArray(DiscreteGene[]::new);

        return new DiscreteChromosome(ISeq.of(discreteGenes));
    }

    public static Experiment readExperiment(JSONObject jsonObject) {
        Experiment experiment = Experiment.getInstance();

        if (valid(jsonObject, "a", "d", "t", "i")) {

            int positions = jsonObject.getInt("a");
            Object distanceObject = jsonObject.get("d");
            List<Double> distances = new ArrayList<>();
            if (distanceObject instanceof List) {
                distances = (List<Double>) distanceObject;
            }

            List<DiscretePoint> treasures = new ArrayList<>();
            jsonObject.getJSONArray("t").forEach(o -> treasures.add(readDiscretePoint((JSONObject) o)));

            List<DiscreteChromosome> individuals = new ArrayList<>();
            jsonObject.getJSONArray("i").forEach(o -> individuals.add(readDiscreteChromosome((JSONObject) o)));

            experiment.setPositions(positions);
            experiment.setDistances(distances);
            experiment.setTreasures(treasures);
            experiment.setIndividuals(individuals);
        }

        return experiment;
    }

    public static boolean valid(JSONObject jsonObject, String... requiredKeys) {
        if (jsonObject.keySet().size() != requiredKeys.length) {
            return false;
        }
        return Set.of(requiredKeys).containsAll(jsonObject.keySet());
    }
}
