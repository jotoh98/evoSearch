package evo.search.view.part;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import evo.search.Main;
import evo.search.io.entities.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class ProjectListItem extends JPanel {
    private JButton deleteButton;
    private JLabel nameLabel;
    private JLabel versionLabel;
    private JLabel pathLabel;

    private Project project;

    public ProjectListItem(Project project, boolean hovered) {
        setupUI();
        this.project = project;
        nameLabel.setText(this.project.getName());
        versionLabel.setText(this.project.getVersion());
        pathLabel.setText(this.project.getPath());

        deleteButton.setIcon(UIManager.getIcon("TextField.search.clear.icon"));
        deleteButton.setVisible(hovered);
        pathLabel.setForeground(UIManager.getColor("Button.disabledText"));
    }

    public static void main(String[] args) {
        Main.setupEnvironment();
        final ProjectListItem projectListItem = new ProjectListItem(new Project("0.0.1", "/jotoh/usr/lol", "Untitled Project 1"), false);
        final JFrame jFrame = new JFrame();
        jFrame.add(projectListItem);
        jFrame.setSize(300, 80);
        jFrame.setMinimumSize(new Dimension(160, 80));
        jFrame.setMaximumSize(new Dimension(500, 80));
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void addDeletionListener(ActionListener actionListener) {
        deleteButton.addActionListener(actionListener);
    }

    private void setupUI() {
        setLayout(new GridLayoutManager(1, 2, new Insets(3, 8, 3, 8), 0, 0));
        setOpaque(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setOpaque(false);
        add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel2.setOpaque(false);
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Label");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(nameLabel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(spacer1, gbc);
        versionLabel = new JLabel();
        versionLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(versionLabel, gbc);
        pathLabel = new JLabel();
        pathLabel.setText("Label");
        panel1.add(pathLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setIconTextGap(0);
        deleteButton.setToolTipText(ResourceBundle.getBundle("lang").getString("delete"));
        deleteButton.setVisible(true);
        add(deleteButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 30), new Dimension(-1, 30), new Dimension(-1, 30), 0, false));
    }

}
