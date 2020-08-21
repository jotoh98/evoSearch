package evo.search.view;

import evo.search.Main;
import evo.search.ga.DiscretePoint;
import evo.search.util.RandomUtils;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Dialog to shuffle elements with a distance range and amount.
 *
 * @param <T> return type of values shuffled by the dialog
 */
public class ShuffleDialog<T> extends JDialog {

    /**
     * Standard minimum distance to shuffle.
     */
    private static final double MIN_DISTANCE = 1.0;

    /**
     * Standard maximum distance to shuffle.
     */
    private static final double MAX_DISTANCE = 9.0;

    /**
     * Standard amount of shuffled elements.
     */
    private static final int AMOUNT = 10;

    static {
        Main.setupEnvironment();
    }

    /**
     * Shuffle process cancel property.
     */
    private final AtomicBoolean processCanceled = new AtomicBoolean(false);
    /**
     * Root pane of the dialog.
     */
    private JPanel contentPane;
    /**
     * Button to start shuffling.
     */
    private JButton buttonOK;
    /**
     * Button to cancel shuffling.
     */
    private JButton buttonCancel;
    /**
     * Spinner of the shuffle amount property.
     */
    private JSpinner amountSpinner;
    /**
     * Shuffle progress bar.
     */
    private JProgressBar shuffleProgressBar;
    /**
     * Input of the minimum distance shuffled.
     */
    private JFormattedTextField minDistanceTextField;
    /**
     * Input of the maximum distance shuffled.
     */
    private JFormattedTextField maxDistanceTextField;
    /**
     * Consumer of the shuffled list.
     */
    @Setter
    private Consumer<List<T>> treasureConsumer = discretePoints -> {
    };

    /**
     * Completable future of the shuffle process.
     */
    private CompletableFuture<List<T>> shuffledFuture = null;

    /**
     * Supplier of random elements with given min and max distance.
     */
    @Setter
    private BiFunction<Double, Double, T> randomSupplier;

    /**
     * Positions amount to choose from.
     */
    @Setter
    private int positions = 2;

    /**
     * Construct a shuffle dialog.
     */
    public ShuffleDialog() {
        setContentPane(contentPane);
        setModal(true);
        setSize(new Dimension(300, 200));
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);

        minDistanceTextField.setValue(MIN_DISTANCE);
        maxDistanceTextField.setValue(MAX_DISTANCE);
        amountSpinner.setValue(AMOUNT);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
    }

    /**
     * Testing entry point of the shuffle dialog.
     *
     * @param args cli args (ignored)
     */
    public static void main(final String[] args) {
        final ShuffleDialog<DiscretePoint> dialog = new ShuffleDialog<>();
        dialog.setRandomSupplier(dialog::shuffleTreasures);
        dialog.setVisible(true);
    }

    /**
     * Shuffle action on button ok click.
     */
    private void onOK() {
        minDistanceTextField.setEnabled(false);
        maxDistanceTextField.setEnabled(false);
        amountSpinner.setEnabled(false);
        buttonOK.setEnabled(false);

        shuffleProgressBar.setVisible(true);
        final double minDistance = ((Number) minDistanceTextField.getValue()).doubleValue();
        final double maxDistance = ((Number) maxDistanceTextField.getValue()).doubleValue();

        final int amount = (int) amountSpinner.getValue();


        shuffleProgressBar.setMaximum(amount);

        shuffledFuture = CompletableFuture
                .supplyAsync(() -> {
                    final ArrayList<T> shuffledTreasures = new ArrayList<>();
                    for (int i = 0; i < amount && !processCanceled.get(); i++) {
                        shuffleProgressBar.setValue(i + 1);
                        shuffledTreasures.add(randomSupplier.apply(minDistance, maxDistance));
                    }
                    return shuffledTreasures;
                });

        shuffledFuture
                .exceptionally(throwable -> null)
                .thenAccept(discretePoints -> {
                    treasureConsumer.accept(discretePoints);
                    close();
                });
    }

    /**
     * Treasure supplier by min and max distance.
     *
     * @param minDistance minimum distance for shuffled treasures
     * @param maxDistance maximum distance for shuffled treasures
     * @return shuffled treasure discrete point
     */
    public DiscretePoint shuffleTreasures(final double minDistance, final double maxDistance) {
        return RandomUtils.generatePoint(positions, minDistance, maxDistance);
    }

    /**
     * Action for cancel button click.
     */
    private void onCancel() {
        if (shuffledFuture != null) {
            processCanceled.set(true);
            shuffledFuture.cancel(true);
        }
        close();
    }

    /**
     * Dialog close action.
     */
    private void close() {
        setVisible(false);
        dispose();
    }

    /**
     * Custom generated text fields.
     */
    private void createUIComponents() {
        minDistanceTextField = new JFormattedTextField(new DecimalFormat());
        maxDistanceTextField = new JFormattedTextField(new DecimalFormat());
    }

}
