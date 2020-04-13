package evo.search.io;

import evo.search.Experiment;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.view.LangService;
import io.jenetics.util.ISeq;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Json service to serialize and deserialize {@link Experiment}s and members.
 */
public class JsonService {

    /**
     * Serialize a {@link DiscretePoint}.
     *
     * @param discretePoint Point to serialize.
     * @return Json object for the {@link DiscretePoint}.
     */
    public static JSONObject write(DiscretePoint discretePoint) {
        return new JSONObject()
                .accumulate("p", discretePoint.getPosition())
                .accumulate("d", discretePoint.getDistance());
    }

    /**
     * Serialize a {@link DiscreteGene}.
     *
     * @param discreteGene Genome to serialize.
     * @return Json object for the {@link DiscreteGene}.
     */
    public static JSONObject write(DiscreteGene discreteGene) {
        return write(discreteGene.getAllele());
    }

    /**
     * Serialize a {@link DiscreteChromosome}.
     *
     * @param discreteChromosome Chromosome to serialize.
     * @return Json object for the {@link DiscreteChromosome}.
     */
    public static JSONObject write(DiscreteChromosome discreteChromosome) {
        JSONObject jsonObject = new JSONObject();
        discreteChromosome.forEach(discreteGene -> jsonObject.append("c", write(discreteGene)));
        return jsonObject;
    }

    /**
     * Serialize an {@link Experiment}.
     *
     * @param experiment Experiment to serialize.
     * @return Json object for the {@link Experiment}.
     */
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

    /**
     * Deserialize an {@link DiscretePoint}s json.
     *
     * @param jsonObject Json to deserialize into an {@link DiscretePoint}.
     * @return A new {@link DiscretePoint} instance.
     */
    public static DiscretePoint readDiscretePoint(JSONObject jsonObject) {
        if (valid(jsonObject, "p", "d")) {
            return new DiscretePoint(jsonObject.getInt("p"), jsonObject.getDouble("d"));
        }
        return null;
    }

    /**
     * Deserialize a {@link DiscretePoint}s json.
     *
     * @param jsonObject Json to deserialize into a {@link DiscretePoint}.
     * @return A new {@link DiscretePoint} instance.
     */
    public static DiscreteGene readDiscreteGene(JSONObject jsonObject) {
        DiscretePoint discretePoint = readDiscretePoint(jsonObject);
        if (discretePoint == null) {
            return null;
        }
        return new DiscreteGene(discretePoint);
    }

    /**
     * Deserialize a {@link DiscreteChromosome}s json.
     *
     * @param jsonObject Json to deserialize into a {@link DiscreteChromosome}.
     * @return A new {@link DiscreteChromosome} instance.
     */
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

    /**
     * Deserialize an {@link Experiment}s json.
     * Writes the experiment directly to the singleton.
     *
     * @param jsonObject Json to deserialize into an {@link Experiment}.
     */
    public static void readExperiment(JSONObject jsonObject) {
        final Experiment experiment = Experiment.getInstance();

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
            EventService.LOG_EVENT.trigger(LangService.get("experiment.loaded"));
        }
    }

    /**
     * Checks if a {@link JSONObject} contains all of the given {@code requiredKeys}.
     *
     * @param jsonObject   Json object to validate.
     * @param requiredKeys Required string keys expected in the json objects keys.
     * @return Whether a json object has all of the {@code requiredKeys}.
     */
    public static boolean valid(JSONObject jsonObject, String... requiredKeys) {
        if (jsonObject.keySet().size() != requiredKeys.length) {
            return false;
        }
        return Set.of(requiredKeys).containsAll(jsonObject.keySet());
    }
}
