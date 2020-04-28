package evo.search.io.entities;

import evo.search.Main;
import lombok.Value;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
public class IndexEntry {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    String path;
    String name;
    String version;
    LocalDateTime lastUsed;

    public static IndexEntry parse(final Element element) {
        Attribute pathAttribute = element.attribute("path");
        Attribute nameAttribute = element.attribute("name");
        Attribute versionAttribute = element.attribute("version");
        Attribute lastUsedAttribute = element.attribute("lastUsed");

        if (pathAttribute == null) {
            return null;
        }
        String path = pathAttribute.getValue();
        String name = nameAttribute == null ? "Unknown" : nameAttribute.getValue();
        String version = versionAttribute == null ? Main.UNKNOWN_VERSION : versionAttribute.getValue();
        LocalDateTime lastUsed = lastUsedAttribute == null ? LocalDateTime.now() : LocalDateTime.parse(lastUsedAttribute.getValue(), DATE_FORMAT);

        return new IndexEntry(path, name, version, lastUsed);
    }

    public Element createElement() {
        return new DefaultElement("entry")
                .addAttribute("path", path)
                .addAttribute("name", name)
                .addAttribute("version", version)
                .addAttribute("lastUsed", DATE_FORMAT.format(lastUsed));

    }
}
