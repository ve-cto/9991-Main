package frc.robot.subsystems.tools;

public class MapRanges {

    double input = 31; // Example input value

    // Source range
    double sourceMin = -31;
    double sourceMax = 31;

    // Target range
    double targetMin = -1;
    double targetMax = 1;

    // Perform the mapping
    double mappedValue = map(input, sourceMin, sourceMax, targetMin, targetMax);

    public double Map(double valToMap, double fromMin, double fromMax, double toMin, double toMax) {
        double mappedValue = map(valToMap, fromMin, fromMax, toMin, toMax);
        return mappedValue;
    }

    public double MapTX(double valToMap) {
        // Source range (-31 to 31 degrees)
        double sourceMin = -31;
        double sourceMax = 31;

        // Output Range (Motors)
        double targetMin = -1;
        double targetMax = 1;

        double mappedValue = map(valToMap, sourceMin, sourceMax, targetMin, targetMax);

        return mappedValue;
    }

    public static double map(double value, double sourceMin, double sourceMax, double targetMin, double targetMax) {
        return targetMin + (value - sourceMin) * (targetMax - targetMin) / (sourceMax - sourceMin);
    }
}
