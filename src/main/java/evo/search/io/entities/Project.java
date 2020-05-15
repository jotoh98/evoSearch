package evo.search.io.entities;

import evo.search.io.service.XmlService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
@RequiredArgsConstructor
public class Project implements XmlEntity {
    String version;
    String path;
    String name;

    int selectedConfiguration = -1;

    List<Configuration> configurations = new ArrayList<>();

    public File getWorkingDirectory() {
        return new File(path);
    }

    @Override
    public Element serialize() {
        DefaultElement projectElement = new DefaultElement("project");
        Arrays.asList(
                XmlService.writeProperty("name", name),
                XmlService.writeProperty("version", version),
                XmlService.writeProperty("path", path),
                XmlService.writeProperty("selectedConfiguration", selectedConfiguration)
        ).forEach(projectElement::add);
        return projectElement;
    }

    @Override
    public void parse(final Element element) {
        XmlService.readProperties(element, (key, value) -> {
            switch (key) {
                case "name":
                    setName(value);
                    break;
                case "version":
                    setVersion(value);
                    break;
                case "path":
                    setPath(value);
            }
        });
    }
}
