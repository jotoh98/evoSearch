package evo.search.io.entities;

import evo.search.io.service.XmlService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
@RequiredArgsConstructor
public class Project implements XmlEntity<Project> {
    String version;
    String path;
    String name;

    List<Configuration> configurations = new ArrayList<>();

    @Override
    public Document serialize() {
        final DefaultElement projectElement = new DefaultElement("project");
        Arrays.asList(
                XmlService.writeProperty("name", name),
                XmlService.writeProperty("version", version),
                XmlService.writeProperty("path", path)
        ).forEach(projectElement::add);
        return DocumentHelper.createDocument(projectElement);
    }

    @Override
    public Project parse(final Document projectSettings) {
        XmlService.readProperties(projectSettings.getRootElement(), (key, value) -> {
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
        return this;
    }
}
