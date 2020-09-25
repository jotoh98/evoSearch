package evo.search.view;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import evo.search.io.service.EventService;
import evo.search.io.service.MenuService;
import evo.search.view.part.Canvas;
import evo.search.view.part.GeneListCellEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author jotoh
 */
public class Preview extends JFrame {
    private static final Evolution EVOLUTION = Evolution.builder().configuration(Configuration.builder().build()).build();
    private JTable geneTable;
    private DefaultTableModel geneTableModel;
    private JPanel rootPanel;
    private JSpinner positionsSpinner;
    private Canvas canvas;
    private JTable fitnessTable;

    public Preview() {
        positionsSpinner.setValue(1);
        positionsSpinner.addChangeListener(l -> {
            final short value = ((Number) positionsSpinner.getValue()).shortValue();

            if (value < 1)
                positionsSpinner.setValue(1);
            final List<DiscreteGene> geneList = getChromosome();
            if (value > 0)
                geneList.forEach(gene -> gene.setPositions(value));
            canvas.clear();
            canvas.setRays(value);
            canvas.render(geneList);
            fillFitnessTable(geneList);
        });
        geneTableModel.addTableModelListener(e -> {
            final List<DiscreteGene> chromosome = getChromosome();
            canvas.clear();
            canvas.render(chromosome);
            fillFitnessTable(chromosome);
        });
        ((DefaultTableModel) fitnessTable.getModel()).setColumnIdentifiers(new Object[]{"Method", "Value"});

        setMenuBar(MenuService.menuBar(
                MenuService.menu(
                        "File",
                        MenuService.item(
                                "Paste",
                                e -> {
                                    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    try {
                                        final String data = (String) clipboard.getData(DataFlavor.stringFlavor);
                                        final Matcher matcher = GeneListCellEditor.PATTERN.matcher(data);
                                        geneTableModel.setRowCount(0);
                                        while (matcher.find())
                                            geneTableModel.addRow(new Object[]{GeneListCellEditor.parseGene(matcher)});
                                    } catch (final UnsupportedFlavorException | IOException ignored) {
                                    }
                                }
                        )
                )
        ));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                EventService.OPEN_CHOOSER_FORM.trigger();
                super.windowClosed(e);
            }
        });
    }

    /**
     * Preview test method.
     *
     * @param ignored ignored cli args
     */
    public static void main(final String[] ignored) {
        Main.setupEnvironment();
        final Preview preview = new Preview();
        preview.positionsSpinner.setValue(5);
        preview.add(new DiscreteGene(5, 0, 5));
        preview.add(new DiscreteGene(5, 4, 5));
        preview.add(new DiscreteGene(5, 3, 5));
        preview.add(new DiscreteGene(5, 2, 5));
        preview.showFrame();
    }

    public void add(final DiscreteGene gene) {
        geneTableModel.addRow(new Object[]{gene});
        positionsSpinner.setValue(gene.getPositions());
    }

    private List<DiscreteGene> getChromosome() {
        final short spinnerValue = ((Number) positionsSpinner.getValue()).shortValue();
        return geneTableModel
                .getDataVector()
                .stream()
                .map(vector -> (DiscreteGene) vector.get(0))
                .map(gene -> new DiscreteGene(spinnerValue, gene.getPosition() % spinnerValue, gene.getDistance()))
                .collect(Collectors.toList());
    }

    public void showFrame() {
        setContentPane(rootPanel);
        pack();
        setVisible(true);
    }

    private void createUIComponents() {
        geneTable = new JTable(new DefaultTableModel(new Object[]{"Gene"}, 0) {
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return DiscreteGene.class;
            }
        });
        geneTableModel = (DefaultTableModel) geneTable.getModel();
        geneTable.setDefaultRenderer(DiscreteGene.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                if (value instanceof DiscreteGene)
                    return super.getTableCellRendererComponent(table, ((DiscreteGene) value).printSmall(), isSelected, hasFocus, row, column);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        final JTextField field = new JTextField();
        geneTable.setDefaultEditor(DiscreteGene.class, new GeneListCellEditor(field));

        geneTable.setTableHeader(null);
        geneTable.addMouseListener(new MouseAdapter() {
            /**
             * Start index of the drag action.
             */
            int dragStart = -1;

            @Override
            public void mousePressed(final MouseEvent e) {
                dragStart = geneTable.rowAtPoint(e.getPoint());
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                final int dragEnd = geneTable.rowAtPoint(e.getPoint());
                if (dragEnd == dragStart) return;
                final Object valueAt = geneTableModel.getValueAt(dragStart, 0);
                geneTableModel.removeRow(dragStart);
                geneTableModel.insertRow(dragEnd, new Object[]{valueAt});
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (geneTable.rowAtPoint(e.getPoint()) > -1) return;
                addNewGene(field);
            }
        });

        geneTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    geneTable.getCellEditor(0, 0).stopCellEditing();
                    final int[] selectedIndices = geneTable.getSelectionModel().getSelectedIndices();
                    for (final int index : selectedIndices)
                        geneTableModel.removeRow(index);
                }
            }
        });

        geneTable.setFillsViewportHeight(true);
        geneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void addNewGene(final JTextField field) {
        geneTableModel.insertRow(geneTableModel.getRowCount(), new Object[]{new DiscreteGene(0, 0, 0)});
        geneTable.setRowSelectionInterval(geneTableModel.getRowCount() - 1, geneTableModel.getRowCount() - 1);
        geneTable.editCellAt(geneTableModel.getRowCount() - 1, 0);
        field.setText("");
        field.requestFocus();
    }

    private void fillFitnessTable(final List<DiscreteGene> chromosome) {
        final DefaultTableModel model = (DefaultTableModel) fitnessTable.getModel();
        model.setRowCount(0);
        for (final Evolution.Fitness fitness : Evolution.Fitness.values()) {
            final Double fitnessValue = fitness.getMethod().apply(EVOLUTION, chromosome);
            model.addRow(new Object[]{fitness.name(), fitnessValue});
        }
        final double optimal = AnalysisUtils.worstCaseSpiralStrategy(chromosome);
        final double worstCase = AnalysisUtils.worstCase(chromosome, 1f);
        model.addRow(new Object[]{"OPTIMAL", optimal});
        model.addRow(new Object[]{"CLOSENESS", worstCase / optimal});
    }
}
