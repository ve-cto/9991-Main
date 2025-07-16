package frc.robot.subsystems.commands;

import frc.robot.subsystems.maps.LimelightMap;

public class LimelightDriveSubsystem {
    private LimelightMap limelightMap = new LimelightMap();

    public void aimAndRange(double desiredArea) {
        // Call the function to get forward and rotation values
        // double[] aimAndRange = limelightMap.limelightAimAndRangeProportional(desiredArea);

        // Pass these values into the drive method
        // return aimAndRange;
    }
    
    public void range(double desiredArea, double rotation, double speed) {
        // double forward = limelightMap.limelightRangeProportional(desiredArea);

        // driveSubsystem.drive(forward, rotation*speed, 1.0);
    }

    public void aim(double forward, double speed) {
        // double rotation = limelightMap.limelightAimProportional();

        // driveSubsystem.drive(forward*speed, rotation, 1.0);
    }
}
