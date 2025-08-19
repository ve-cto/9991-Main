package frc.robot.subsystems.commands;
import frc.robot.LimelightHelpers;
import frc.robot.subsystems.tools.MapRanges;
import frc.robot.subsystems.commands.DriveSubsystem;

public class Limelight {
    private double limelightTA;
    private double limelightTX;
    private double limelightTY;

    MapRanges mapRanges = new MapRanges();

    private String Limelight = "limelight";

    public Limelight() {}

    public void periodic() {
        limelightTA = LimelightHelpers.getTA(Limelight);
        limelightTX = LimelightHelpers.getTX(Limelight);
        limelightTY = LimelightHelpers.getTY(Limelight);
    }

    public double getAimMotorOutput(double offset) {    
        // control the intensity of the robots movements: too high = oscilate, too low = not enough power to move
        double kP = 0.4;

        double targetingAngleUnmapped = (LimelightHelpers.getTX("limelight") + offset) * kP;

        double targetDesiredRotationSpeed = 10*mapRanges.MapTX(targetingAngleUnmapped);

        //invert since tx is positive when the target is to the right of the crosshair
        // targetDesiredRotationSpeed *= -1.0;

        return targetDesiredRotationSpeed;
    }

    public double getRangeMotorOutput(double desiredArea) {    
        double kP = 0.1;
        double error = desiredArea - limelightTA;
        double targetingForwardSpeed = 0;

        // Check if it's valid
        if (limelightTA > 0) {
            targetingForwardSpeed = mapRanges.Map(error * kP, -100 * kP, 100 * kP, -1, 1);
        }
        return targetingForwardSpeed;
    }
}
