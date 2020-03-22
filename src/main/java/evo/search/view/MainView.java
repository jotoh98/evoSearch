package evo.search.view;

import lombok.Getter;

import javax.swing.*;

@Getter
public class MainView {

    private Canvas canvas = new Canvas();
    private JFrame mainFrame = new JFrame();

    public MainView() {
        mainFrame.setSize(500, 500);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setContentPane(canvas);
        mainFrame.setVisible(true);
    }
}
