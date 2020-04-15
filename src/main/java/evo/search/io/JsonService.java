package evo.search.io;

import evo.search.Configuration;
import evo.search.Environment;
import evo.search.Run;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.util.ISeq;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Json service to serialize and deserialize {@link Environment}s and members.
 */
public class JsonService {

    private static final String VERSION = "ver";
    private static final String DISTANCE = "distance";
    private static final String DISTANCES = "distance";
    private static final String POSITION = "position";
    private static final String POSITIONS = "positions";
    private static final String TREASURES = "treasures";
    private static final String HISTORY = "history";
    private static final String CHROMOSOME = "chromosome";
    private static final String LIMIT = "limit";

    private static String[] CONFIG_JSON = new String[]{"ver", "positions", "distances", "treasures", "history"};

    /**
     * Serialize a {@link DiscretePoint}.
     *
     * @param discretePoint Point to serialize.
     * @return Json object for the {@link DiscretePoint}.
     */
    public static JSONObject write(DiscretePoint discretePoint) {
        return new JSONObject()
                .accumulate(POSITION, discretePoint.getPosition())
                .accumulate(DISTANCE, discretePoint.getDistance());
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
        discreteChromosome.forEach(discreteGene -> jsonObject.append(CHROMOSOME, write(discreteGene)));
        return jsonObject;
    }

    /**
     * Serialize a {@link Run}.
     *
     * @param run Run to serialize.
     * @return Json object for the {@link Run}.
     */
    public static JSONObject write(Run run) {
        return write(run.getIndividual())
                .accumulate(LIMIT, run.getLimit());
    }

    /**
     * Serialize an {@link Environment}.
     *
     * @param configuration Experiment to serialize.
     * @return Json object for the {@link Environment}.
     */
    public static JSONObject write(Configuration configuration) {
        JSONObject jsonObject = new JSONObject()
                .accumulate(VERSION, configuration.getVersion())
                .accumulate(POSITIONS, configuration.getPositions())
                .accumulate(DISTANCES, configuration.getDistances())
                .accumulate(LIMIT, configuration.getLimit());

        if (configuration.getTreasures().size() == 0) {
            jsonObject.put(TREASURES, new ArrayList<>());
        }
        if (configuration.getHistory().size() == 0) {
            jsonObject.put(HISTORY, new ArrayList<>());
        }

        configuration.getTreasures().forEach(discretePoint -> jsonObject.append(TREASURES, write(discretePoint)));
        configuration.getHistory().forEach(discreteChromosome -> jsonObject.append(HISTORY, write(discreteChromosome)));
        return jsonObject;
    }

    /**
     * Deserialize an {@link DiscretePoint}s json.
     *
     * @param jsonObject Json to deserialize into an {@link DiscretePoint}.
     * @return A new {@link DiscretePoint} instance.
     */
    public static DiscretePoint readDiscretePoint(JSONObject jsonObject) {
        if (invalid(jsonObject, POSITION, DISTANCE)) {
            return null;
        }
        return new DiscretePoint(jsonObject.getInt(POSITION), jsonObject.getDouble(DISTANCE));
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
        if (invalid(jsonObject, CHROMOSOME)) {
            return null;
        }
        List<DiscreteGene> genes = new ArrayList<>();
        jsonObject.getJSONArray(CHROMOSOME).forEach(
                o -> genes.add(readDiscreteGene((JSONObject) o))
        );
        DiscreteGene[] discreteGenes = genes.stream()
                .filter(Objects::nonNull)
                .toArray(DiscreteGene[]::new);

        return new DiscreteChromosome(ISeq.of(discreteGenes));
    }

    public static Run readRun(JSONObject jsonObject) {
        if (invalid(jsonObject, LIMIT, CHROMOSOME)) {
            return null;
        }
        final int limit = jsonObject.getInt(LIMIT);
        final DiscreteChromosome discreteChromosome = readDiscreteChromosome(jsonObject.getJSONObject(CHROMOSOME));
        return new Run(limit, discreteChromosome);
    }

    /**
     * Deserialize an {@link Environment}s json.
     * Writes the experiment directly to the singleton.
     *
     * @param jsonObject Json to deserialize into an {@link Environment}.
     * @return The saved configuration.
     */
    public static Configuration readConfiguration(JSONObject jsonObject) {
        if (invalid(jsonObject, CONFIG_JSON)) {
            return null;
        }

        String version = jsonObject.getString(VERSION);
        int positions = jsonObject.getInt(POSITIONS);

        final List<Double> distances = new ArrayList<>();
        final Object distanceObject = jsonObject.get(DISTANCES);
        if (distanceObject instanceof List) {
            distances.addAll((List<Double>) distanceObject);
        }

        final List<DiscretePoint> treasures = new ArrayList<>();
        jsonObject.getJSONArray(TREASURES).forEach(o -> treasures.add(readDiscretePoint((JSONObject) o)));

        final List<Run> history = new ArrayList<>();
        jsonObject.getJSONArray(HISTORY).forEach(o -> history.add(readRun((JSONObject) o)));

        final Configuration configuration = new Configuration(version, positions, distances, treasures);

        configuration.getHistory().addAll(history);
        return configuration;
    }

    /**
     * Checks if a {@link JSONObject} misses one of the given {@code requiredKeys}.
     *
     * @param jsonObject   Json object to validate.
     * @param requiredKeys Required string keys expected in the json objects keys.
     * @return Whether a json object has of the {@code requiredKeys} missing.
     */
    public static boolean invalid(JSONObject jsonObject, String... requiredKeys) {
        if (jsonObject.keySet().size() != requiredKeys.length) {
            return true;
        }
        return !Set.of(requiredKeys).containsAll(jsonObject.keySet());
    }
}
