package evo.search.experiments;

public abstract class Experiment {
    public static void printProgress(int progress, final int amount) {
        progress = Math.min(amount, progress);

        System.out.println("|" + "-".repeat(progress) + " ".repeat(amount - progress) + "|\r");
    }
}
