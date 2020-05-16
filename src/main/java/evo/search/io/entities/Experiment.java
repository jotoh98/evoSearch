package evo.search.io.entities;

import evo.search.io.service.XmlService;
import evo.search.view.LangService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Experiment implements XmlEntity {
    private String name = LangService.get("unnamed");
    private Configuration configuration;
    private List<Run> history;

    public Experiment(Configuration configuration) {
        this(LangService.get("unnamed"), configuration, new ArrayList<>());
    }

    public String getVersion() {
        return configuration.getVersion();
    }

    @Override
    public Element serialize() {
        Element rootElement = DocumentHelper.createElement("experiment");
        rootElement.addAttribute("name", name);
        rootElement.add(configuration.serialize());
        XmlService.appendElementList(rootElement.addElement("runs"), history, XmlEntity::serialize);
        return rootElement;
    }

    @Override
    public void parse(final Element element) {
        Element configurationElement = element.element("configuration");
        Attribute nameAttribute = element.attribute("name");
        Element runsElement = element.element("runs");

        if (nameAttribute != null) {
            name = nameAttribute.getValue();
        }
        if (configurationElement != null) {
            configuration = Configuration.parseConfiguration(configurationElement);
        }

        if (runsElement != null) {
            XmlService.forEach("run", runsElement, runElement -> history.add(Run.parseRun(runElement)));
        }
    }

    public void clearHistory() {
        history = new ArrayList<>();
    }
}
