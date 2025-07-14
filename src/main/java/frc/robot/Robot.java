package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.commands.LimelightDriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import frc.robot.subsystems.tools.MapRanges;
import frc.robot.subsystems.maps.LimelightMap;
import frc.robot.subsystems.commands.Elevator;

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
  
  private static double driveSpeedSlow = 0.6;
  private static double driveSpeedNormal = 0.7;
  private static double driveSpeedFast = 0.8;
  private static double driveSpeedMax = 1.0;
  private double driveSpeedCurrent;

  private ControllerMap controllerMap;
  private DriveSubsystem driveSubsystem;
  private MapRanges mapRanges;
  private LimelightDriveSubsystem limelightDriveSubsystem;
  private LimelightMap limelightMap;
  private Elevator elevator;

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
  WPI_VictorSPX m_intake = new WPI_VictorSPX(8);


  public Robot() {}

  @Override
  public void robotInit() {
    // Create an instance of controllerMap and driveSubsystem
    controllerMap = new ControllerMap();
    driveSubsystem = new DriveSubsystem();
    mapRanges = new MapRanges();
    limelightMap = new LimelightMap();
    limelightDriveSubsystem = new LimelightDriveSubsystem();
    elevator = new Elevator();

    // set preventDrive to false on init
    this.preventDrive = false;
  

    SmartDashboard.getBoolean("Prevent Driver Control?", preventDrive);
    SmartDashboard.getBoolean("Use Joysticks to Drive?", useJoystickDrive);

    autoChooser.setDefaultOption("Default Auto", autoDefault);
    autoChooser.addOption("Custom 1", autoCustom1);
    SmartDashboard.putData("Pick an auto.", autoChooser);

    driveSchemeChooser.setDefaultOption("Single-Controller Control", driveSchemeDefault);
    driveSchemeChooser.addOption("Dual-Controller Control", driveSchemeDual);
    driveSchemeChooser.addOption("Joystick Control", driveSchemeJoystick);
    SmartDashboard.putData("selectedDriveMode", driveSchemeChooser);

    elevator.reset();
  }

  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Current Speed", driveSpeedCurrent);

    SmartDashboard.putString("Current Elevator Position", elevator.getPosition().toString());
    SmartDashboard.putNumber("Current Elevator Height", elevator.getHeight());
    SmartDashboard.putString("Elevator ranging towards:", elevator.getTargetPosition().toString());
    SmartDashboard.putBoolean("Elevator Endstop", elevator.getEndstop());
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

    driveSchemeSelected = driveSchemeChooser.getSelected();
    SmartDashboard.putString("Current drive scheme:", DRIVE_SCHEME_STRINGS[driveSchemeSelected]);
  }

  @Override
  public void teleopPeriodic() {
    // Reset all the motors to 0 (They will be changed later in the cycle, so this is temporary.)
    double m_intakeSet = 0.0;
    double m_shooterSet = 0.0;
    double forward = 0.0;
    double rotation = 0.0;
    driveSpeedCurrent = driveSpeedNormal;


    if (controllerMap.isLeftDPadC1Pressed()) {
      elevator.gotoL1();
    } else if (controllerMap.isUpDPadC1Pressed()) {
      elevator.gotoL2();
    } else if (controllerMap.isRightDPadC1Pressed()) {
      elevator.gotoL3();
    } else if (controllerMap.isDownDPadC1Pressed()) {
      elevator.gotoL0();
    } else if (controllerMap.isNoDPadC1Pressed()) {
      elevator.home();
    } else {
      elevator.feed();
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

    // Set the motors to their values in the main class.
    m_shooter.set(this.m_shooterSet);
    m_intake.set(this.m_intakeSet);




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

      SmartDashboard.putNumber("Drive Forward Value", forward);
      SmartDashboard.putNumber("Drive Rotation Value", rotation);
      SmartDashboard.putNumber("Drive Speed", driveSpeedCurrent);

      // if (controllerMap.isStartButtonC1Pressed()) {
      //   // Aim and Range on Start Button
      //   limelightDriveSubsystem.aimAndRange(40);
      // } else if (controllerMap.isRightStickButtonC1Pressed()) {
      //   // Aim on Right stick
      //   limelightDriveSubsystem.aim(forward, driveSpeedCurrent);
      // } else if (controllerMap.isLeftStickButtonC1Pressed()) {
      //   // Range on Left Stick
      //   limelightDriveSubsystem.range(50, rotation, driveSpeedCurrent);
      // } else if (!controllerMap.isLeftStickButtonC1Pressed() && !controllerMap.isRightStickButtonC1Pressed() && !controllerMap.isStartButtonC1Pressed()) {
      //   // If not currently using a limelight action, drive normally
      //   driveSubsystem.drive(forward, rotation, driveSpeedCurrent);
      //   System.out.println("Driving with joysticks...");
      // } 

      driveSubsystem.drive(forward, rotation, driveSpeedCurrent);
    } else {
      driveSubsystem.drive(0.0, 0.0, 0.0);
    }
    // System.out.println("Drive method has been called");
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
