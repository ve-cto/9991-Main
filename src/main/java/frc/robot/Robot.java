package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.commands.LimelightDriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import frc.robot.subsystems.tools.MapRanges;
import frc.robot.subsystems.maps.LimelightMap;
import frc.robot.subsystems.commands.Elevator;
import frc.robot.subsystems.commands.EndEffector;

// import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
  private double driveSpeedCurrent;
  private double forward;
  private double rotation;

  private ControllerMap controllerMap;
  private DriveSubsystem driveSubsystem;
  private MapRanges mapRanges;
  private LimelightDriveSubsystem limelightDriveSubsystem;
  private LimelightMap limelightMap;
  private Elevator elevator;
  private EndEffector endEffector;

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
    endEffector = new EndEffector();

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

    SmartDashboard.putString("Intake Status", endEffector.getCoralState());
    SmartDashboard.putBoolean("Coral Loaded?", endEffector.getCoralLoaded());

    SmartDashboard.putNumber("Drive Forward Value", forward);
    SmartDashboard.putNumber("Drive Rotation Value", rotation);

    SmartDashboard.putNumber("Encoder Distance", elevator.getHeightRaw());
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
    driveSchemeSelected = driveSchemeChooser.getSelected();
    SmartDashboard.putString("Current drive scheme:", DRIVE_SCHEME_STRINGS[driveSchemeSelected]);
    elevator.reset();
  }

  @Override
  public void teleopPeriodic() {
    // Reset the driving vars
    double forward = 0.0;
    double rotation = 0.0;
    driveSpeedCurrent = Constants.Robot.driveSpeedNormal;


    // -------------------------------------------------------------------------------------------------------
    // ELEVATOR
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isLeftDPadC1Pressed()) {
      elevator.gotoL2();
    } else if (controllerMap.isUpDPadC1Pressed()) {
      elevator.gotoL3();
    } else if (controllerMap.isRightDPadC1Pressed()) {
      elevator.gotoL4();
    } else if (controllerMap.isDownDPadC1Pressed()) {
      elevator.gotoL1();
    } else if (controllerMap.isNoDPadC1Pressed()) {
      elevator.hold();
    } else if (controllerMap.isStartButtonC1Pressed()) {
      elevator.home();
    }

    // if (controllerMap.isUpDPadC1Pressed()) {
    //   elevator.manualShift(0.3);
    // } else if (controllerMap.isDownDPadC1Pressed()) {
    //   elevator.manualShift(-0.3);
    // } else {
    //   elevator.hold();
    // }
    
    // -------------------------------------------------------------------------------------------------------
    // END EFFECTOR
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isAButtonC1Pressed()) {
      // endEffector.intakeCoral();
      endEffector.manualShift(0.4);
    } else if (controllerMap.isBButtonC1Pressed()) {
      // endEffector.releaseCoral();
      endEffector.manualShift(-0.4);
    } else {
      endEffector.stop();
    }


    // -------------------------------------------------------------------------------------------------------
    // DRIVE
    // -------------------------------------------------------------------------------------------------------
    // Handle Triggers for Drive Speed
    if (controllerMap.isLeftTriggerC1Pressed() && !controllerMap.isRightTriggerC1Pressed()) {
      driveSpeedCurrent = Constants.Robot.driveSpeedSlow;
    } else if (controllerMap.isRightTriggerC1Pressed() && !controllerMap.isLeftTriggerC1Pressed()) {
      driveSpeedCurrent = Constants.Robot.driveSpeedFast;
    } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
      // If both of the triggers are held at the same time, max the motors.
      driveSpeedCurrent = Constants.Robot.driveSpeedMax;
    }

    // Arcadedrive the robot using the selected drive scheme
    if (driveSchemeSelected == 0) {
      forward = controllerMap.getRightXC1();
      rotation = -controllerMap.getLeftYC1();
    } else if (driveSchemeSelected == 1) {
      forward = controllerMap.getRightXC2();
      rotation = -controllerMap.getLeftYC2();
    } else if (driveSchemeSelected == 2) {
      forward = controllerMap.getJoystickAxes(0);
      rotation = controllerMap.getJoystickAxes(1);
    } else {
      forward = 0;
      rotation = 0;
      System.out.print("Strangely, a drive scheme could not be selected, and an error occured.");
    }

    

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

    this.forward = forward;
    this.rotation = rotation;
    driveSubsystem.drive(forward, rotation, driveSpeedCurrent);
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
