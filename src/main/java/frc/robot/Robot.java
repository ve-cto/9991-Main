package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.subsystems.ControllerMap;

import frc.robot.subsystems.DriveSubsystem;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.net.PortForwarder;
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
  private SparkMax m_intake;
  private SparkMax m_loader;
  private SparkMax m_shooter;
  private SparkMax m_climber;
  private DigitalInput noteEndstop;

  private double m_intakeSet;
  private double m_shooterSet;
  private double m_loaderSet;
  private double m_climberSet;
  
  private static double driveSpeedSlow = 0.6;
  private static double driveSpeedNormal = 0.7;
  private static double driveSpeedFast = 0.8;
  private static double driveSpeedMax = 1.0;
  private double driveSpeedCurrent;

  private ControllerMap controllerMap;
  private DriveSubsystem driveSubsystem;

  private boolean preventDrive;

  

  public Robot() {
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Initialize the SPARKs
    // Control Motors
    m_shooter = new SparkMax(1, MotorType.kBrushless);
    m_loader = new SparkMax(2, MotorType.kBrushless);
    m_climber = new SparkMax(3, MotorType.kBrushless);
    m_intake = new SparkMax(8, MotorType.kBrushless);
    
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Create an instance of controllerMap and driveSubsystem
    controllerMap = new ControllerMap();
    driveSubsystem = new DriveSubsystem();

    // set preventDrive to false
    this.preventDrive = false;

    // setup the endstop
    noteEndstop = new DigitalInput(0);
  
    // forward port 5800-5809 to the limelight (useful for USB control)
    for (int port = 5800; port <= 5809; port++) {
        PortForwarder.add(port, "limelight.local", port);
    }
  }

  @Override
  public void robotPeriodic() {
    // Display the applied output of the left and right side onto the dashboard
    // SmartDashboard.putNumber("Left Out", m_leftLeader.getAppliedOutput());
    // SmartDashboard.putNumber("Right Out", m_rightLeader.getAppliedOutput());

    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");

    //read values periodically
    double x = tx.getDouble(0.0);
    double y = ty.getDouble(0.0);
    double area = ta.getDouble(0.0);

    //post to smart dashboard periodically
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
    // Reset all the motors to 0 (They will be changed later in the cycle, so this is temporary.)
    double m_intakeSet = 0.0;
    double m_shooterSet = 0.0;
    double m_loaderSet = 0.0;
    double m_climberSet = 0.0;
    driveSpeedCurrent = driveSpeedNormal;


    // Handle Action Buttons
    if (controllerMap.isAButtonC1Pressed() && !controllerMap.isBButtonC1Pressed()) {
      // A button is pressed, but not B button (Spin up shooter)
      m_shooterSet = 1.0;
    } else if (controllerMap.isBButtonC1Pressed() && controllerMap.isAButtonC1Pressed()) {
      // B button AND a button is pressed (Full shoot)
      m_shooterSet = 1.0;
      m_intakeSet = -0.7;
      m_loaderSet = 1.0;
    } else if (controllerMap.isXButtonC1Pressed() && !controllerMap.isAButtonC1Pressed() && !controllerMap.isYButtonC1Pressed()) {
    // X button is pressed, but not A or Y (prevent override of variable) (suck in note)
      m_shooterSet = -0.5;
      m_loaderSet = -0.5;
    } else if (controllerMap.isYButtonC1Pressed() && !controllerMap.isAButtonC1Pressed() && !controllerMap.isXButtonC1Pressed()) {
    // Y button is pressed, but not A, Y or X (drop into amp)
      m_shooterSet = -0.4;
      m_loaderSet = -0.4;
      m_intakeSet = -0.3;
    }

    // Handle Bumpers
    // Left Bumper (Eject)
    if (controllerMap.isLeftBumperC1Pressed() && !controllerMap.isBButtonC1Pressed() && !controllerMap.isYButtonC1Pressed() && !controllerMap.isRightBumperC1Pressed()) {
      m_intakeSet = 0.6;
    // Right Bumper (Intake)
    } else if (controllerMap.isRightBumperC1Pressed() && !controllerMap.isBButtonC1Pressed() && !controllerMap.isYButtonC1Pressed() && !controllerMap.isLeftBumperC1Pressed()) {
      m_intakeSet = -0.6;
    }

    // Handle Triggers
    if (controllerMap.isLeftTriggerC1Pressed() && !controllerMap.isRightTriggerC1Pressed()) {
      driveSpeedCurrent = driveSpeedSlow;
    } else if (controllerMap.isRightTriggerC1Pressed() && !controllerMap.isLeftTriggerC1Pressed()) {
      driveSpeedCurrent = driveSpeedFast;
    } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
      // If both of the triggers are held at the same time, max the motors.
      driveSpeedCurrent = driveSpeedMax;
    }

    // Take the desired motor values, and push them to this (main class).
    this.m_shooterSet = m_shooterSet;
    this.m_intakeSet = m_intakeSet;
    this.m_loaderSet = m_loaderSet;
    this.m_climberSet = m_climberSet;

    // Set the motors to their values in the main class.
    m_shooter.set(this.m_shooterSet);
    m_intake.set(this.m_intakeSet);
    m_loader.set(this.m_loaderSet);
    m_climber.set(this.m_climberSet);

    // Check to see if preventDrive is true: if it is, stop control to the drive motors
    if (!preventDrive) {
      // Arcadedrive the robot
      double forward = -controllerMap.getLeftYC1();
      double rotation = controllerMap.getRightXC1();
      driveSubsystem.drive(forward, rotation, driveSpeedCurrent);
    }
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
