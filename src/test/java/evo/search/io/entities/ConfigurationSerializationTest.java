package evo.search.io.entities;

import org.dom4j.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationSerializationTest {

    @Test
    void serializationTest() {
        final Configuration build = Configuration.builder().build();
        final Document serializedDocument = build.serialize();

        final Configuration parsed = new Configuration().parse(serializedDocument);
        //TODO: fix equals over config alterers (this$alterers missing) to cut .toString() method
        assertEquals(build.toString(), parsed.toString());
    }
}