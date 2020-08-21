package evo.search.view.part;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import evo.search.Main;
import evo.search.io.entities.IndexEntry;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Project display for the {@link evo.search.view.ChooserForm}.
 */
public class ProjectListItem extends JPanel {

    /**
     * Index entry displayed in the item.
     */
    private final IndexEntry entry;

    /**
     * Item and entry deletion button.
     */
    private JButton deleteButton;

    /**
     * Label to display the project's version.
     */
    private JLabel versionLabel;

    /**
     * Label to display the project's path.
     */
    private JLabel pathLabel;

    /**
     * Label to display the project's name.
     */
    @Getter
    private JLabel nameLabel;

    /**
     * Create a project list item from an index entry.
     *
     * @param indexEntry index entry to display
     */
    public ProjectListItem(final IndexEntry indexEntry) {
        setupUI();
        entry = indexEntry;
        nameLabel.setText(entry.getName());
        versionLabel.setText(entry.getVersion());
        pathLabel.setText(entry.getPath());

        final boolean exists = Files.exists(Path.of(indexEntry.getPath()));

        deleteButton.setIcon(UIManager.getIcon("TextField.search.clear.icon"));
        deleteButton.setVisible(false);
        pathLabel.setForeground(UIManager.getColor("Button.disabledText"));

        if (!exists)
            nameLabel.setForeground(Color.RED);

        final ProjectListItem listItem = this;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                listItem.setBackground(UIManager.getColor("List.selectionBackground"));
                deleteButton.setVisible(true);
                versionLabel.setVisible(false);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                final Component exitTarget = findComponentAt(e.getPoint());
                if (exitTarget != null && SwingUtilities.isDescendingFrom(exitTarget, listItem)) {
                    return;
                }

                setBackground(null);
                deleteButton.setVisible(false);
                versionLabel.setVisible(true);
                super.mouseExited(e);
            }
        });

        final int height = 40;
        setMinimumSize(new Dimension(100, height));
        setPreferredSize(new Dimension(120, height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    /**
     * Test program entry point.
     *
     * @param args cli args (ignored)
     */
    public static void main(final String[] args) {
        Main.setupEnvironment();
        final ProjectListItem projectListItem = new ProjectListItem(new IndexEntry("0.0.1", "/jotoh/usr/lol", "Untitled Project 1", LocalDateTime.now()));
        final JFrame jFrame = new JFrame();
        jFrame.add(projectListItem);
        jFrame.setSize(300, 80);
        jFrame.setMinimumSize(new Dimension(160, 80));
        jFrame.setMaximumSize(new Dimension(500, 80));
        jFrame.pack();
        jFrame.setVisible(true);
    }

    /**
     * Bind a given deletion event listener to the delete button.
     *
     * @param actionListener deletion event listener
     */
    public void bindDeleteEvent(final ActionListener actionListener) {
        deleteButton.addActionListener(actionListener);
    }

    /**
     * Bind a given selection event listener to the click event of the item.
     *
     * @param projectSelection selection event listener
     */
    public void bindSelectionEvent(final Consumer<IndexEntry> projectSelection) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                projectSelection.accept(entry);
            }
        });
    }

    /**
     * Set up the ui for the list item.
     */
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
        // deepcode ignore Missing~add~javax.swing.JPanel: Panel is spacer
        final JPanel spacer1 = new JPanel();
        spacer1.setOpaque(false);
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
