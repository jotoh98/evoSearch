package evo.search.util;

public class MathUtils {

    public static double distance(double angleA, double distanceA, double angleB, double distanceB) {
        double subtract = 2 * distanceA * distanceB * Math.cos(angleA - angleB);
        return Math.sqrt(distanceA * distanceA + distanceB * distanceB - subtract);
    }

    public static double areaInTriangle(final double a, final double b, final double c) {
        final double s = (a + b + c) / 2d;
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

}
