package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.commands.LimelightDriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import frc.robot.subsystems.tools.MapRanges;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
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
  private MapRanges mapRanges;
  private LimelightDriveSubsystem limelightDriveSubsystem;

  private boolean preventDrive;
  private boolean useJoystickDrive;

  private static final int driveSchemeDefault = 0;
  private static final int driveSchemeDual = 1;
  private static final int driveSchemeJoystick = 2;
  private static final String[] DRIVE_SCHEME_STRINGS = {"Single-Controller", "Dual-Controller", "Joystick"};
  private int driveSchemeSelected;
  private final SendableChooser<Integer> driveSchemeChooser = new SendableChooser<>();

  private static final String autoDefault = "Default";
  private static final String autoCustom1 = "Custom1";
  private String autoSelected;
  private final SendableChooser<String> autoChooser = new SendableChooser<>();
  
  WPI_VictorSPX m_shooter = new WPI_VictorSPX(3);
  WPI_VictorSPX m_loader = new WPI_VictorSPX(1);
  WPI_VictorSPX m_climber = new WPI_VictorSPX(2);
  WPI_VictorSPX m_intake = new WPI_VictorSPX(8);


  public Robot() {
    // Create an instance of controllerMap and driveSubsystem
    controllerMap = new ControllerMap();
    driveSubsystem = new DriveSubsystem();
    mapRanges = new MapRanges();
    limelightDriveSubsystem = new LimelightDriveSubsystem();

    // set preventDrive to false on init
    this.preventDrive = false;

    // setup the endstop
    noteEndstop = new DigitalInput(0);
  
    // forward port 5800-5809 to the limelight (useful for USB control)
    for (int port = 5800; port <= 5809; port++) {
        PortForwarder.add(port, "limelight.local", port);
    }

    SmartDashboard.getBoolean("Prevent Driver Control?", preventDrive);
    SmartDashboard.getBoolean("Use Joysticks to Drive?", useJoystickDrive);

    autoChooser.setDefaultOption("Default Auto", autoDefault);
    autoChooser.addOption("Custom 1", autoCustom1);
    SmartDashboard.putData("Pick an auto.", autoChooser);

    driveSchemeChooser.setDefaultOption("Single-Controller Control", driveSchemeDefault);
    driveSchemeChooser.addOption("Dual-Controller Control", driveSchemeDual);
    driveSchemeChooser.addOption("Joystick Control", driveSchemeJoystick);
    SmartDashboard.putData("selectedDriveMode", driveSchemeChooser);
  }

  @Override
  public void robotPeriodic() {
    // Display the applied output of the left and right side onto the dashboard
    // SmartDashboard.putNumber("Left Out", m_leftLeader.getAppliedOutput());
    // SmartDashboard.putNumber("Right Out", m_rightLeader.getAppliedOutput());

    boolean noteEndstopStatus = noteEndstop.get();
    SmartDashboard.putBoolean("Note Endstop Status", noteEndstopStatus);

    SmartDashboard.putNumber("Current Speed", driveSpeedCurrent);

    
    // SmartDashboard.putNumber("Axis 0", controllerMap.getJoystickAxes(0));
    // SmartDashboard.putNumber("Axis 1", controllerMap.getJoystickAxes(1));
    // SmartDashboard.putNumber("Axis 2", controllerMap.getJoystickAxes(2));
    // SmartDashboard.putNumber("Axis 3", controllerMap.getJoystickAxes(3));
    // SmartDashboard.putNumber("Axis 4", controllerMap.getJoystickAxes(4));
    // SmartDashboard.putNumber("Axis Count", controllerMap.getAxisCount());
  }

  @Override
  public void autonomousInit() {
    autoSelected = autoChooser.getSelected();
    SmartDashboard.putString("Currently running auto:", autoSelected);
  }

  @Override
  public void autonomousPeriodic() {
    switch (autoSelected) {
      case autoCustom1:
        // code in here...
        break;
      case autoDefault:
      default:
        // code in here...
        break;
    }
  }

  @Override
  public void teleopInit() {
    // Reset all the motors to 0, after auto is completed
    this.m_intakeSet = 0.0;
    this.m_shooterSet = 0.0;
    this.m_loaderSet = 0.0;
    this.m_climberSet = 0.0;

    driveSchemeSelected = driveSchemeChooser.getSelected();
    SmartDashboard.putString("Current drive scheme:", DRIVE_SCHEME_STRINGS[driveSchemeSelected]);
  }

  @Override
  public void teleopPeriodic() {
    // Reset all the motors to 0 (They will be changed later in the cycle, so this is temporary.)
    double m_intakeSet = 0.0;
    double m_shooterSet = 0.0;
    double m_loaderSet = 0.0;
    double m_climberSet = 0.0;
    double forward;
    double rotation;
    driveSpeedCurrent = driveSpeedNormal;


    // Handle Buttons
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
    // Right Bumper (Intake until endstop)
    } else if (controllerMap.isRightBumperC1Pressed() && !controllerMap.isBButtonC1Pressed() && !controllerMap.isYButtonC1Pressed() && !controllerMap.isLeftBumperC1Pressed() && !noteEndstop.get()) {
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

    // Take the desired motor values, and push them to this. This forces them to reset at the end of an auto, and means that they are public and accessible in other classes
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
      // Arcadedrive the robot using the selected drive scheme
      if (driveSchemeSelected == 0 || driveSchemeSelected == 1) {
        forward = controllerMap.getRightXC1();
        rotation = -controllerMap.getLeftYC1();
      } else if (driveSchemeSelected == 2) {
        forward = controllerMap.getJoystickAxes(0);
        rotation = controllerMap.getJoystickAxes(1);
      } else {
        forward = 0;
        rotation = 0;
        System.out.print("Strangely, a drive scheme could not be selected, or an error occured.");
      }

      if (controllerMap.isRightStickButtonC1Pressed()) {
        // Aim and range on right-stick
        limelightDriveSubsystem.aimAndRange(40);
      } else if (controllerMap.isLeftStickButtonC1Pressed()) {
        // Aim on left-stick
        limelightDriveSubsystem.aim(0.0);
      } else {
        // If not currently using a limelight action, drive normally
        driveSubsystem.drive(forward, rotation, driveSpeedCurrent);
      }
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
