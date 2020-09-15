package evo.search.io.entities;

import evo.search.io.service.XmlService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Project entity to be saved in the file system.
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class Project implements XmlEntity<Project> {

    /**
     * Projects version number for file compatibility.
     */
    String version;

    /**
     * Projects file system path.
     */
    Path path;

    /**
     * Projects name.
     */
    String name;

    /**
     * List of projects registered configurations.
     */
    List<Configuration> configurations = new ArrayList<>();

    @Override
    public Element serialize() {
        final DefaultElement projectElement = new DefaultElement("project");
        Arrays.asList(
                XmlService.writeProperty("name", name),
                XmlService.writeProperty("version", version),
                XmlService.writeProperty("path", path.toString())
        ).forEach(projectElement::add);
        return projectElement;
    }

    @Override
    public Project parse(final Element projectSettings) {
        XmlService.readProperties(projectSettings, (key, value) -> {
            switch (key) {
                case "name":
                    setName(value);
                    break;
                case "version":
                    setVersion(value);
                    break;
                case "path":
                    setPath(Path.of(value));
            }
        });
        return this;
    }

}
