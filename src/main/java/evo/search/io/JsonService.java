package evo.search.io;

import evo.search.Environment;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Experiment;
import evo.search.view.LangService;
import io.jenetics.util.ISeq;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Json service to serialize and deserialize {@link Environment}s and members.
 */
public class JsonService {

    private static final String VERSION = "ver";
    private static final String NAME = "name";
    private static final String DISTANCE = "distance";
    private static final String DISTANCES = "distance";
    private static final String POSITION = "position";
    private static final String POSITIONS = "positions";
    private static final String TREASURES = "treasures";
    private static final String INDIVIDUALS = "individuals";
    private static final String CHROMOSOME = "chromosome";
    private static final String LIMIT = "limit";
    private static final String CONFIGURATION = "configuration";

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
     * Serialize a {@link Configuration}.
     *
     * @param configuration Configuration to serialize.
     * @return Json object for the {@link Configuration}.
     */
    public static JSONObject write(Configuration configuration) {
        JSONObject jsonObject = new JSONObject()
                .accumulate(VERSION, configuration.getVersion())
                .accumulate(NAME, configuration.getName())
                .accumulate(POSITIONS, configuration.getPositions())
                .accumulate(DISTANCES, configuration.getDistances())
                .accumulate(LIMIT, configuration.getLimit());

        if (configuration.getTreasures().size() == 0) {
            jsonObject.put(TREASURES, new ArrayList<>());
        }

        configuration.getTreasures().forEach(discretePoint -> jsonObject.append(TREASURES, write(discretePoint)));
        return jsonObject;
    }

    /**
     * Serialize an {@link Experiment}.
     *
     * @param experiment Experiment to serialize.
     * @return Json object for the {@link Experiment}.
     */
    public static JSONObject write(Experiment experiment) {
        final JSONObject jsonObject = new JSONObject()
                .accumulate(CONFIGURATION, write(experiment.getConfiguration()));
        experiment.getIndividuals().forEach(run -> jsonObject.append(INDIVIDUALS, write(run)));
        return jsonObject;
    }

    /**
     * Deserialize an {@link DiscretePoint}s json.
     *
     * @param jsonObject Json to deserialize into an {@link DiscretePoint}.
     * @return A new {@link DiscretePoint} instance.
     */
    public static DiscretePoint readDiscretePoint(JSONObject jsonObject) {
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
     * @param jsonArray Json to deserialize into a {@link DiscreteChromosome}.
     * @return A new {@link DiscreteChromosome} instance.
     */
    public static DiscreteChromosome readDiscreteChromosome(JSONArray jsonArray) {
        List<DiscreteGene> genes = new ArrayList<>();
        jsonArray.forEach(
                o -> genes.add(readDiscreteGene((JSONObject) o))
        );
        DiscreteGene[] discreteGenes = genes.stream()
                .filter(Objects::nonNull)
                .toArray(DiscreteGene[]::new);

        return new DiscreteChromosome(ISeq.of(discreteGenes));
    }

    public static Experiment readExperiment(JSONObject jsonObject) {
        final Configuration configuration = readConfiguration(jsonObject.getJSONObject(CONFIGURATION));

        final ArrayList<DiscreteChromosome> individuals = new ArrayList<>();

        jsonObject.getJSONArray(INDIVIDUALS).forEach(object -> {
            if (object instanceof JSONArray) {
                individuals.add(readDiscreteChromosome((JSONArray) object));
            }
        });

        return new Experiment(configuration, individuals);
    }

    /**
     * Deserialize an {@link Configuration}s json.
     * Writes the experiment directly to the singleton.
     *
     * @param jsonObject Json to deserialize into an {@link Configuration}.
     * @return The saved configuration.
     */
    public static Configuration readConfiguration(JSONObject jsonObject) {
        String version = "undefined";
        try {
            version = jsonObject.getString(VERSION);
        } catch (JSONException e) {
            EventService.LOG_LABEL.trigger(LangService.get("version.undefined"));
            EventService.LOG.trigger(LangService.get("version.undefined"));
        }

        final String name = jsonObject.getString(NAME);

        int positions = jsonObject.getInt(POSITIONS);

        final List<Double> distances = new ArrayList<>();
        final Object distanceObject = jsonObject.get(DISTANCES);
        if (distanceObject instanceof List) {
            distances.addAll((List<Double>) distanceObject);
        }

        final List<DiscretePoint> treasures = new ArrayList<>();
        jsonObject.getJSONArray(TREASURES).forEach(o -> treasures.add(readDiscretePoint((JSONObject) o)));

        return new Configuration(version, name, 1000, positions, distances, treasures, Environment::fitness);
    }
}
