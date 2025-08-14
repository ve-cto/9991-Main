// package frc.robot.subsystems.maps;
// import frc.robot.LimelightHelpers;
// import frc.robot.subsystems.tools.MapRanges;

// public class LimelightMap {
//     MapRanges mapRanges = new MapRanges();

//     private String Limelight = "limelight";


//     // public double limelightAimProportional() {    
//     //     // control the intensity of the robots movements: too high = oscilate, too low = not enough power to move
//     //     double kP = 0.1;

//     //     double targetingAngleUnmapped = LimelightHelpers.getTX("limelight") * kP;

//     //     double targetDesiredRotationSpeed = mapRanges.MapTX(targetingAngleUnmapped) * kP;

//     //     //invert since tx is positive when the target is to the right of the crosshair
//     //     targetDesiredRotationSpeed *= -1.0;

//     //     return targetDesiredRotationSpeed;
//     // }

//     // public double limelightRangeProportional(double desiredArea) {    
//     //     double kP = 0.1;
//     //     double error = desiredArea - limelightTA;
//     //     double targetingForwardSpeed = 0;

//     //     // Check if it's valid
//     //     if (limelightTA > 0) {
//     //         targetingForwardSpeed = mapRanges.Map(error * kP, -100 * kP, 100 * kP, -1, 1);
//     //     }
//     //     return targetingForwardSpeed;
//     // }

//     // public double[] limelightAimAndRangeProportional(double desiredArea) {
//     //     double forward = 0;
//     //     double rotation = 0;
//     //     double kP = 0.1;
//     //     double error = desiredArea - limelightTA;


//     //     // Check if it's valid
//     //     if (limelightTA > 0) {
//     //         forward = mapRanges.Map(error * kP, -100 * kP, 100 * kP, -1, 1);
//     //         rotation = mapRanges.MapTX(limelightTX)*kP;
//     //     }

//     //     double[] returnValues = {forward, rotation};
//     //     return returnValues;
//     // }
// }
