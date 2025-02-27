package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class DriveSubsystem {
    private SparkMax m_leftLeader;
    private SparkMax m_leftFollower;
    private SparkMax m_rightLeader;
    private SparkMax m_rightFollower;
    private DifferentialDrive m_robotDrive;

    public DriveSubsystem() {
        // Drive Motors
        m_rightLeader = new SparkMax(4, MotorType.kBrushless);
        m_rightFollower = new SparkMax(5, MotorType.kBrushless);
        m_leftLeader = new SparkMax(6, MotorType.kBrushless);
        m_leftFollower = new SparkMax(7, MotorType.kBrushless);
        // group
        m_robotDrive = new DifferentialDrive(m_leftLeader, m_rightLeader);
    
        /*
     * Create new SPARK MAX configuration objects. These will store the
     * configuration parameters for the SPARK MAXes that we will set below.
     */
    SparkMaxConfig globalConfig = new SparkMaxConfig();
    SparkMaxConfig rightLeaderConfig = new SparkMaxConfig();
    SparkMaxConfig leftFollowerConfig = new SparkMaxConfig();
    SparkMaxConfig rightFollowerConfig = new SparkMaxConfig();

    /*
     * Set parameters that will apply to all SPARKs. We will also use this as
     * the left leader config.
     */
    globalConfig
        .smartCurrentLimit(50)
        .idleMode(IdleMode.kBrake);

    // Apply the global config and invert since it is on the opposite side
    rightLeaderConfig
        .apply(globalConfig)
        .inverted(true);

    // Apply the global config and set the leader SPARK for follower mode
    leftFollowerConfig
        .apply(globalConfig)
        .follow(m_leftLeader);

    // Apply the global config and set the leader SPARK for follower mode
    rightFollowerConfig
        .apply(globalConfig)
        .follow(m_rightLeader);

    /*
     * Apply the configuration to the SPARKs.
     *
     * kResetSafeParameters is used to get the SPARK MAX to a known state. This
     * is useful in case the SPARK MAX is replaced.
     *
     * kPersistParameters is used to ensure the configuration is not lost when
     * the SPARK MAX loses power. This is useful for power cycles that may occur
     * mid-operation.
     */
    m_leftLeader.configure(globalConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    m_leftFollower.configure(leftFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    m_rightLeader.configure(rightLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    m_rightFollower.configure(rightFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    }

    /*
     * Drive the robot.
     * Speed is multiplied by forward and rotation. If you want absolute values, use the "Max" speed.
     */
    public void drive(Double forward, Double rotation, Double speed) {
        m_robotDrive.arcadeDrive(forward*speed, rotation*speed);
    }


}
