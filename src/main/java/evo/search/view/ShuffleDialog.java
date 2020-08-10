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
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ShuffleDialog<T> extends JDialog {

    private static final double MIN_DISTANCE = 1.0;
    private static final double MAX_DISTANCE = 9.0;
    private static final int AMOUNT = 10;

    static {
        Main.setupEnvironment();
    }

    private final AtomicBoolean okPressed = new AtomicBoolean(false);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner amountSpinner;
    private JProgressBar shuffleProgressBar;
    private JFormattedTextField minDistanceTextField;
    private JFormattedTextField maxDistanceTextField;
    private final AtomicBoolean processCanceled = new AtomicBoolean(false);
    @Setter
    private Consumer<List<T>> treasureConsumer = discretePoints -> {
    };
    private CompletableFuture<List<T>> shuffledFuture = null;
    @Setter
    private BiFunction<Double, Double, T> randomSupplier;
    @Setter
    private int positions = 2;

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

    public static void main(final String[] args) {
        final ShuffleDialog<DiscretePoint> dialog = new ShuffleDialog<>();
        dialog.setRandomSupplier(dialog::shuffleTreasures);
        dialog.setVisible(true);
    }

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

        okPressed.set(true);
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
                    okPressed.set(false);
                    treasureConsumer.accept(discretePoints);
                    close();
                });
    }

    public DiscretePoint shuffleTreasures(final double minDistance, final double maxDistance) {
        return RandomUtils.generatePoint(positions, minDistance, maxDistance);
    }

    private void onCancel() {
        if (shuffledFuture != null) {
            processCanceled.set(true);
            shuffledFuture.cancel(true);
        }
        close();
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(final String path, final String key) {
        ResourceBundle bundle;
        try {
            final Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                final Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (final Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    private void createUIComponents() {
        minDistanceTextField = new JFormattedTextField(new DecimalFormat());
        maxDistanceTextField = new JFormattedTextField(new DecimalFormat());
    }
}
