package evo.search.io.entities;

import evo.search.Main;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global project register entry used to save registered projects in the file system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexEntry implements XmlEntity<IndexEntry> {

    /**
     * Date format used to display the {@link #lastUsed} property.
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * File system path of the project.
     */
    Path path = Path.of("/");

    /**
     * Name of the project.
     */
    String name = "Unknown";

    /**
     * Version of the project.
     */
    String version = "unknown";

    /**
     * Time since last project use.
     */
    LocalDateTime lastUsed = LocalDateTime.MIN;

    /**
     * Parse an {@link IndexEntry} from an {@link Element}.
     *
     * @param element element to parse
     * @return parsed index entry
     */
    public IndexEntry parse(final Element element) {
        final Attribute pathAttribute = element.attribute("path");
        final Attribute nameAttribute = element.attribute("name");
        final Attribute versionAttribute = element.attribute("version");
        final Attribute lastUsedAttribute = element.attribute("lastUsed");

        if (pathAttribute == null) {
            return null;
        }
        path = Path.of(pathAttribute.getValue());
        name = nameAttribute == null ? "Unknown" : nameAttribute.getValue();
        version = versionAttribute == null ? Main.UNKNOWN_VERSION : versionAttribute.getValue();
        lastUsed = lastUsedAttribute == null ? LocalDateTime.now() : LocalDateTime.parse(lastUsedAttribute.getValue(), DATE_FORMAT);

        return this;
    }

    /**
     * Serialize an {@link IndexEntry} to an {@link Element}.
     *
     * @return serialized index entry
     */
    public Element serialize() {
        return new DefaultElement("entry")
                .addAttribute("path", path.toString())
                .addAttribute("name", name)
                .addAttribute("version", version)
                .addAttribute("lastUsed", DATE_FORMAT.format(lastUsed));

    }

}
