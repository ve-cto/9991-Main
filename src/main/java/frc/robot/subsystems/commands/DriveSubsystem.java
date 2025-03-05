package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class DriveSubsystem {
    private WPI_VictorSPX m_leftLeader;
    private WPI_VictorSPX m_leftFollower;
    private WPI_VictorSPX m_rightLeader;
    private WPI_VictorSPX m_rightFollower;
    private DifferentialDrive m_robotDrive;

    public DriveSubsystem() {
        // Drive Motors
        m_rightLeader = new WPI_VictorSPX(4);
        m_rightFollower = new WPI_VictorSPX(5);
        m_leftLeader = new WPI_VictorSPX(6);
        m_leftFollower = new WPI_VictorSPX(7);

        // Set followers
        m_rightFollower.follow(m_rightLeader);
        m_leftFollower.follow(m_leftLeader);

        // group
        m_robotDrive = new DifferentialDrive(m_leftLeader, m_rightLeader);
    }

    // Drive the robot.
    // Speed is multiplied by forward and rotation. 
    // The speeds are as follows:
    //  driveSlowSpeed
    //  driveNormalSpeed
    //  driveFastSpeed
    //  driveMaxSpeed
    //  driveCurrentSpeed (Defined from controller triggers)
    // If you want absolute values, use the "Max" speed.
    public void drive(Double forward, Double rotation, Double speed) {
        m_robotDrive.arcadeDrive(forward * speed, rotation * speed);
    }
}
