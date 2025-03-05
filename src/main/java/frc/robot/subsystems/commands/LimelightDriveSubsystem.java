package frc.robot.subsystems.commands;

import frc.robot.subsystems.maps.LimelightMap;

public class LimelightDriveSubsystem {
    private LimelightMap limelightMap = new LimelightMap();
    private DriveSubsystem driveSubsystem = new DriveSubsystem();

    public void aimAndRange(double desiredArea) {
        // Call the function to get forward and rotation values
        double[] aimAndRange = limelightMap.limelightAimAndRangeProportional(desiredArea);
    
        // Get the values from the array created by lielightAimAndRangeProportional
        double forward = aimAndRange[0];
        double rotation = aimAndRange[1];
    
        // Pass these values into the drive method
        driveSubsystem.drive(forward, rotation, 1.0);
    }
    
    public void range(double desiredArea, double rotation) {
        double forward = limelightMap.limelightRangeProportional(desiredArea);

        driveSubsystem.drive(forward, rotation, 1.0);
    }

    public void aim(double forward) {
        double rotation = limelightMap.limelightAimProportional();

        driveSubsystem.drive(forward, rotation, 1.0);
    }
}
