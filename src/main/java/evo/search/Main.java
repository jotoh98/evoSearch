package evo.search;

import evo.search.view.MainForm;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        MainForm.main(args);
        /*Experiment.init(20, 3);
        final File file = FileService.promptForSave();

        if(file == null)
            return;

        FileService.saveExperiment(file, Experiment.getInstance());

        final File load = FileService.promptForLoad();

        if(load == null)
            return;

        final Experiment experiment = FileService.loadExperiment(load);

        log.info("{}", Experiment.getInstance());
        log.info("{}", experiment);*/

        /*Experiment.init(10, 2);
        JSONObject experimentBuild = JsonService.build(Experiment.getInstance());
        log.info("{}", experimentBuild);

        log.info("{}", JsonService.readExperiment(experimentBuild));


        Experiment.getInstance().setDistances(Arrays.asList(1.5, 1.7, 7.1, 5.0));
        JSONObject build = JsonService.build(DiscreteChromosome.shuffle());
        log.info("{}", build.toString());
        DiscreteChromosome o = JsonService.readDiscreteChromosome(build);
        log.info("{}, {}", o, o.toSeq());
        o.toSeq().forEach(discreteGene -> log.info("{}", discreteGene.getAllele()));
        */
    }


}
