package evo.search.io.entities;

import org.dom4j.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationSerializationTest {

    @Test
    void serializationTest() {
        Configuration build = Configuration.builder().build();
        Document serializedDocument = build.serialize();

        Configuration parsed = new Configuration().parse(serializedDocument);
        //TODO: fix equals over config alterers (this$alterers missing) to cut .toString() method
        assertEquals(build.toString(), parsed.toString());
    }
}