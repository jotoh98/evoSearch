package evo.search.view;

import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.view.listener.DocumentAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Dialog to copy individuals into the clipboard.
 * Manages and stores formatting options.
 */
public class ShareDialog extends JDialog {

    private static final List<DiscretePoint> EXAMPLE = List.of(
            new DiscretePoint(6, 3, 15.234902306498),
            new DiscretePoint(6, 0, 3.5645732213)
    );
    /**
     * Chromosomes to share.
     */
    private final List<DiscreteChromosome> shared = new ArrayList<>();
    ShareMode mode = ShareMode.SINGLE;
    /**
     * Root content pane.
     */
    private JPanel contentPane;
    /**
     * Ok Button. Invokes copy action.
     */
    private JButton buttonOK;
    /**
     * Cancel Button. Cancels copy action.
     */
    private JButton buttonCancel;
    /**
     * Textfield to enter a text format.
     */
    private JTextField formatField;
    /**
     * Label to preview formatted output.
     */
    private JLabel previewLabel;
    /**
     * Separator textfield.
     */
    private JTextField separatorField;
    /**
     * Sharing format for the serialized chromosomes.
     */
    private String fullFormat = "";
    /**
     * Display formatting. Shortens distance output.
     */
    private String displayFormat = "";

    /**
     * Constructor for the main dialog.
     */
    public ShareDialog() {
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        updateFormat();
        formatField.getDocument().addDocumentListener((DocumentAdapter) e -> updateFormat());
        separatorField.getDocument().addDocumentListener((DocumentAdapter) e -> updateFormat());
    }

    public static void main(final String[] args) {
        Main.setupEnvironment();
        final ShareDialog dialog = new ShareDialog();
        dialog.showDialog();
        System.exit(0);
    }

    /**
     * Assign just a single chromosome to the share dialog.
     *
     * @param chromosome single chromosome to share
     */
    public void share(final DiscreteChromosome chromosome) {
        mode = ShareMode.SINGLE;
        shared.add(chromosome);
    }

    private void updateFormat() {
        try {
            final String proposedFormat = formatField.getText();
            String format = proposedFormat.replace("%P", "%1$s");
            format = format.replace("%p", "%2$s");
            this.displayFormat = format.replace("%d", "%3$.2f...");
            this.fullFormat = format.replace("%d", "%3$f");
            updatePreview();
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            previewLabel.setText("Incorrect format");
        }
    }

    private void updatePreview() {
        try {
            final String list = format(
                    Stream.of(
                            new DiscretePoint(6, 3, 15.234902306498),
                            new DiscretePoint(6, 0, 3.5645732213)
                    ),
                    true
            );
            previewLabel.setText(list);
        } catch (final IllegalFormatException ignored) {
            previewLabel.setText("Incorrect format");
        }
    }

    private String format(final DiscretePoint p, final boolean display) throws IllegalFormatException {
        return String.format(Locale.US, display ? displayFormat : fullFormat, p.getPositions(), p.getPosition(), p.getDistance());
    }

    private String format(final Stream<DiscretePoint> points, final boolean display) throws IllegalFormatException {
        final String separator = separatorField.getText();
        return points.map(p -> format(p, display))
                .reduce((s1, s2) -> s1 + separator + s2)
                .orElse("");
    }

    private void onOK() {
        try {
            final String export = shared.stream().map(chromosome -> format(chromosome.getGenes().stream().map(DiscreteGene::getAllele), false))
                    .reduce((s1, s2) -> s1 + "\n" + s2)
                    .orElse("");
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final StringSelection selection = new StringSelection(export);
            clipboard.setContents(selection, selection);

            dispose();
        } catch (final IllegalFormatException ignored) {
            previewLabel.setText("Incorrect format");
        }
    }

    private void onCancel() {
        dispose();
    }

    public void showDialog() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Sharing mode of the dialog.
     */
    private enum ShareMode {
        /**
         * Share only one chromosome.
         */
        SINGLE,
        /**
         * Share multiple chromosomes.
         */
        MULTIPLE
    }
}
