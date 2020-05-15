package evo.search.io.entities;

import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationSerializationTest {

    @Test
    void serializationTest() {
        Configuration build = Configuration.builder().build();
        Element element = build.serialize();

        Configuration parsed = Configuration.parseConfiguration(element);
        //TODO: fix equals over config alterers (this$alterers missing) to cut .toString() method
        assertEquals(build.toString(), parsed.toString());
    }
}