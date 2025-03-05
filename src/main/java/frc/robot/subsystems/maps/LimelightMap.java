package frc.robot.subsystems.maps;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.robot.LimelightHelpers;
import frc.robot.subsystems.tools.MapRanges;

public class LimelightMap {
    MapRanges mapRanges = new MapRanges();

    String Limelight = "limelight";

    LimelightHelpers limelightHelpers = new LimelightHelpers();

    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");

    int tc = LimelightHelpers.getTargetCount(Limelight);

    double[] robotPose = LimelightHelpers.getTargetPose_CameraSpace(Limelight);

    // simple proportional turning control with Limelight.
    // "proportional control" is a control algorithm in which the output is proportional to the error.
    // in this case, we are going to return an angular velocity that is proportional to the 
    // "tx" value from the Limelight.
    double limelight_aim_proportional() {    
        // control the intensity of the robots movements: too high = oscilate, too low = not enough power to move
        double kP = .035;

        double targetingAngleUnmapped = LimelightHelpers.getTX("limelight") * kP;

        double targetDesiredRotationSpeed = mapRanges.MapTX(targetingAngleUnmapped) * kP;

        //invert since tx is positive when the target is to the right of the crosshair
        targetDesiredRotationSpeed *= -1.0;

        return targetDesiredRotationSpeed;
    }

    // simple proportional ranging control with Limelight's "ty" value
    // this works best if your Limelight's mount height and target mount height are different.
    // if your limelight and target are mounted at the same or similar heights, use "ta" (area) for target ranging rather than "ty"
    double limelight_range_proportional() {    
        double kP = .1;
        double targetingForwardSpeed = LimelightHelpers.getTY("limelight") * kP;
        targetingForwardSpeed *= -1.0;
        return targetingForwardSpeed;
    }
}
