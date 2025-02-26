package frc.robot;

import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.robot.subsystems.ControllerMap;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.HttpCamera;
import edu.wpi.first.cscore.HttpCamera.HttpCameraKind;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
  private SparkMax m_leftLeader;
  private SparkMax m_leftFollower;
  private SparkMax m_rightLeader;
  private SparkMax m_rightFollower;
  private SparkMax m_intake;
  private SparkMax m_loader;
  private SparkMax m_shooter;
  private SparkMax m_climber;
  private DigitalInput noteEndstop;

  private double m_intakeSet;
  private double m_shooterSet;
  private double m_loaderSet;
  private double m_climberSet;
  
  private double driveSpeedSlow;
  private double driveSpeedNormal;
  private double driveSpeedFast;
  private double driveSpeedMax;
  private double driveSpeedCurrent;

  private DifferentialDrive m_robotDrive;
  private ControllerMap controllerMap;

  

  public Robot() {
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Initialize the SPARKs
    // Control Motors
    m_shooter = new SparkMax(1, MotorType.kBrushless);
    m_loader = new SparkMax(2, MotorType.kBrushless);
    m_climber = new SparkMax(3, MotorType.kBrushless);
    m_intake = new SparkMax(8, MotorType.kBrushless);
    
    // Drive Motors
    m_rightLeader = new SparkMax(4, MotorType.kBrushless);
    m_rightFollower = new SparkMax(5, MotorType.kBrushless);
    m_leftLeader = new SparkMax(6, MotorType.kBrushless);
    m_leftFollower = new SparkMax(7, MotorType.kBrushless);
    
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

    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Intialize the controllermap
    controllerMap = new ControllerMap();
    
    

    // Initialize the Limelight camera stream
    HttpCamera limelightFeed = new HttpCamera("limelight", "http://10.TE.AM.11:5800/stream.mjpg", HttpCameraKind.kMJPGStreamer);
    CameraServer.startAutomaticCapture(limelightFeed);

    this.driveSpeedSlow = 0.6;
    this.driveSpeedNormal = 0.7;
    this.driveSpeedFast = 0.8;
    this.driveSpeedMax = 0.9;

    noteEndstop = new DigitalInput(0);
  }

  @Override
  public void robotPeriodic() {
    // Display the applied output of the left and right side onto the dashboard
    // SmartDashboard.putNumber("Left Out", m_leftLeader.getAppliedOutput());
    // SmartDashboard.putNumber("Right Out", m_rightLeader.getAppliedOutput());

    // Access Limelight values
    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");

    // Read values periodically
    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double area = ta.getDouble(0.0);

    // Post to SmartDashboard periodically
    SmartDashboard.putNumber("LimelightX", x);
    SmartDashboard.putNumber("LimelightY", y);
    SmartDashboard.putNumber("LimelightArea", area);

    boolean noteEndstopStatus = noteEndstop.get();
    SmartDashboard.putBoolean("Note Endstop Status", noteEndstopStatus);
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    this.m_intakeSet = 0.0;
    this.m_shooterSet = 0.0;
    this.m_loaderSet = 0.0;
    this.m_climberSet = 0.0;
  }

  @Override
  public void teleopPeriodic() {
    double m_intakeSet = 0.0;
    double m_shooterSet = 0.0;
    double m_loaderSet = 0.0;
    double m_climberSet = 0.0;

    // Handle Action Buttons
    if (controllerMap.isAButtonC1Pressed() && !controllerMap.isBButtonC1Pressed()) {
      m_shooterSet = 1.0;
    } else if (controllerMap.isBButtonC1Pressed() && controllerMap.isAButtonC1Pressed()) {
      m_shooterSet = 1.0;
      m_intakeSet = -0.7;
      m_loaderSet = 1.0;
    } else if (controllerMap.isXButtonC1Pressed() && !controllerMap.isAButtonC1Pressed()) {
      m_shooterSet = -0.5;
      m_loaderSet = -0.5;
    } else if (controllerMap.isYButtonC1Pressed() && !controllerMap.isAButtonC1Pressed() && !controllerMap.isXButtonC1Pressed()) {
      m_shooterSet = -0.4;
      m_loaderSet = -0.4;
      m_intakeSet = -0.3;
    } else if (controllerMap.isLeftBumperC1Pressed() && controllerMap.isRightBumperC1Pressed()) {
      m_intakeSet = 0.0;
    } else {
      m_shooterSet = 0.0;
      m_climberSet = 0.0;
      m_loaderSet = 0.0;
    }

    // Handle Bumpers
    if (controllerMap.isLeftBumperC1Pressed() && !controllerMap.isBButtonC1Pressed() && !controllerMap.isYButtonC1Pressed() && !controllerMap.isRightBumperC1Pressed()) {
      m_intakeSet = 0.6;
    } else if (controllerMap.isRightBumperC1Pressed() && !controllerMap.isBButtonC1Pressed() && !controllerMap.isYButtonC1Pressed() && !controllerMap.isLeftBumperC1Pressed()) {
      m_intakeSet = -0.6;
    }


    // Handle Triggers
    if (controllerMap.isLeftTriggerC1Pressed() && !controllerMap.isRightTriggerC1Pressed()) {
      driveSpeedCurrent = driveSpeedSlow;
    } else if (controllerMap.isRightTriggerC1Pressed() && !controllerMap.isLeftTriggerC1Pressed()) {
      driveSpeedCurrent = driveSpeedFast;
    } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
      driveSpeedCurrent = driveSpeedMax;
    } else {
      driveSpeedCurrent = driveSpeedNormal;
    }

    this.m_shooterSet = m_shooterSet;
    this.m_intakeSet = m_intakeSet;
    this.m_loaderSet = m_loaderSet;
    this.m_climberSet = m_climberSet;


    m_shooter.set(this.m_shooterSet);
    m_intake.set(this.m_intakeSet);
    m_loader.set(this.m_loaderSet);
    m_climber.set(this.m_climberSet);

    // Arcadedrive the robot
    double forward = -controllerMap.getLeftYC1()*driveSpeedCurrent;
    double rotation = controllerMap.getRightXC1()*driveSpeedCurrent;
    m_robotDrive.arcadeDrive(forward, rotation);
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void testInit() {
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }
}
