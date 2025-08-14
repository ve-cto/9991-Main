// package frc.robot.subsystems.commands;

// import frc.robot.LimelightHelpers;
// import frc.robot.subsystems.maps.LimelightMap;
// import frc.robot.subsystems.tools.MapRanges;

// public class LimelightDriveSubsystem {
//     private LimelightMap limelightMap = new LimelightMap();
//     MapRanges mapRanges = new MapRanges();

//     private final String LimelightID = "limelight";

//     private double[] robotPose = LimelightHelpers.getTargetPose_CameraSpace(LimelightID);

//     private double limelightTA;
//     private double limelightTY;
//     private double limelightTX;
//     private int limelightTC;

//     private final double aimKP = 0.1;
//     private final double rangeKP = 0.1;

//     LimelightDriveSubsystem() {}

//     public void periodic() {
//         limelightTC = LimelightHelpers.getTargetCount(LimelightID);

//         if (limelightTC > 1) {

//         }
        
//         limelightTA = LimelightHelpers.getTA(LimelightID);
//         limelightTY = LimelightHelpers.getTY(LimelightID);
//         limelightTX = LimelightHelpers.getTX(LimelightID);
//     }

//     public int getTargetCount() {
//         return limelightTC;
//     }

//     public double getTA() {
//         return limelightTA;
//     } 
//     public double getTY() {
//         return limelightTY;
//     }
//     public double getTX() {
//         return limelightTX;
//     }
//     public double[] getPose() {
//         return robotPose;
//     }


//     public double aimToTarget() {    
//         double targetingAngleUnmapped = LimelightHelpers.getTX("limelight") * aimKP;

//         double targetDesiredRotationSpeed = mapRanges.MapTX(targetingAngleUnmapped) * aimKP;

//         //invert since tx is positive when the target is to the right of the crosshair
//         targetDesiredRotationSpeed *= -1.0;


//         return targetDesiredRotationSpeed;
//     }

//     public double limelightRangeProportional(double desiredArea) {    
//         double kP = 0.1;
//         double error = desiredArea - limelightTA;
//         double targetingForwardSpeed = 0;

//         // Check if it's valid
//         if (limelightTA > 0) {
//             targetingForwardSpeed = mapRanges.Map(error * kP, -100 * kP, 100 * kP, -1, 1);
//         }
//         return targetingForwardSpeed;
//     }

//     public double[] limelightAimAndRangeProportional(double desiredArea) {
//         double forward = 0;
//         double rotation = 0;
//         double kP = 0.1;
//         double error = desiredArea - limelightTA;


//         // Check if it's valid
//         if (limelightTA > 0) {
//             forward = mapRanges.Map(error * kP, -100 * kP, 100 * kP, -1, 1);
//             rotation = mapRanges.MapTX(limelightTX)*kP;
//         }

//         double[] returnValues = {forward, rotation};
//         return returnValues;
//     }

//     public double[] aimAndRange(double desiredArea) {
//         // Call the function to get forward and rotation values
//         double[] aimAndRange = limelightAimAndRangeProportional(desiredArea);

//         // Pass these values into the drive method
//         return aimAndRange;
//     }
    
//     public void range(double desiredArea, double rotation, double speed) {
//         double forward = limelightRangeProportional(desiredArea);

//         driveSubsystem.drive(forward, rotation*speed, 1.0);
//     }

//     public void aim(double forward, double speed) {
//         double rotation = limelightAimProportional();

//         driveSubsystem.drive(forward*speed, rotation, 1.0);
//     }
// }
