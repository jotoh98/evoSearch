package evo.search.io.service;

import evo.search.io.entities.Project;
import org.dom4j.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

class XmlServiceTest {

    @Test
    void interactiveRegisterTest() {

        File file = FileService.promptForDirectory();

        Document document = XmlService.writeRegister(ProjectService.getProjects());

        try (FileWriter fileWriter = new FileWriter(file.getPath() + File.separator + "config.xml")) {
            document.write(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Project> projects = XmlService.readRegister(document);
        System.out.println(projects.size());
    }
}