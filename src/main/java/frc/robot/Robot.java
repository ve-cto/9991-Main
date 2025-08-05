package frc.robot;

import com.ctre.phoenix6.signals.ConnectedMotorValue;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.commands.LimelightDriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import frc.robot.subsystems.tools.MapRanges;
import frc.robot.subsystems.maps.LimelightMap;
import frc.robot.subsystems.commands.Elevator;
import frc.robot.subsystems.commands.EndEffector;
import frc.robot.subsystems.commands.Algae;
import frc.robot.subsystems.commands.Led;

// import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
  private ControllerMap controllerMap;
  private DriveSubsystem driveSubsystem;
  private MapRanges mapRanges;
  private LimelightDriveSubsystem limelightDriveSubsystem;
  private LimelightMap limelightMap;
  private Elevator elevator;
  private EndEffector endEffector;
  private Algae algae;
  private Led led;
  private Constants.Led.StatusList ledBuffer;
  private Constants.Led.StatusList ledTeleopBuffer;
  private Timer timer;

  private double driveSpeedCurrent;
  private double forward;
  private double rotation;
  private boolean preventDrive;
  private boolean useJoystickDrive;

  private boolean ledFlashOverride = false;
  private boolean wasCoralLoaded;

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

  private final NetworkTableInstance networkTableInstance = NetworkTableInstance.getDefault();
    
  private final NetworkTable driveTable = networkTableInstance.getTable("Drive");
  private final NetworkTable elevatorTable =  networkTableInstance.getTable("Elevator");
  private final NetworkTable endEffectorTable = networkTableInstance.getTable("EndEffector");
  private final NetworkTable limelightTable = networkTableInstance.getTable("Limelight");
  private final NetworkTable ledTable = networkTableInstance.getTable("LED's");
  private final NetworkTable autonomousTable = networkTableInstance.getTable("Autonomous");

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
    algae = new Algae();
    led = new Led();
    
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
    ledBuffer = Constants.Led.StatusList.BLANK; 
  }

  @Override
  public void robotPeriodic() {
    (driveTable.getDoubleTopic("Drive Forward Value").publish()).set(forward);
    (driveTable.getDoubleTopic("Drive Rotation Value").publish()).set(rotation);
    (driveTable.getDoubleTopic("Drive Speed").publish()).set(driveSpeedCurrent);

    (elevatorTable.getStringTopic("Elevator Position").publish()).set(elevator.getPosition().toString());
    (elevatorTable.getDoubleTopic("Elevator Raw Height").publish()).set(elevator.getHeightRaw());
    (elevatorTable.getDoubleTopic("Elevator Height").publish()).set(elevator.getHeight());
    (elevatorTable.getStringTopic("Elevator Ranging Towards").publish()).set(elevator.getTargetPosition().toString());
    (elevatorTable.getBooleanTopic("Elevator Endstop").publish()).set(elevator.getEndstop());
    
    (endEffectorTable.getStringTopic("Intake Status").publish()).set(endEffector.getCoralState());
    (endEffectorTable.getBooleanTopic("Coral Loaded?").publish()).set(endEffector.getCoralLoaded());

    (ledTable.getStringTopic("LED Status").publish()).set(led.getStatus().toString());
    (ledTable.getBooleanTopic("LED's Flashing?").publish()).set(led.getFlashing());

    if (DriverStation.isDSAttached() == true) {  
      if (ledFlashOverride == false) {
        led.setStatus(ledBuffer);
      }
    } else {
      led.setStatus(Constants.Led.StatusList.DISCONNECT);
    }
    led.periodic();
  }

  @Override
  public void autonomousInit() {
    autoSelected = autoChooser.getSelected();
    
    (autonomousTable.getStringTopic("Running Auto").publish()).set(autoSelected);
  }

  @Override
  public void autonomousPeriodic() {
    led.setStatus(Constants.Led.StatusList.AUTONOMOUS);
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
    (driveTable.getStringTopic("Drive Scheme").publish()).set(DRIVE_SCHEME_STRINGS[driveSchemeSelected]);
    elevator.reset();
  }

  @Override
  public void teleopPeriodic() {
    ledTeleopBuffer = Constants.Led.StatusList.IDLE;

    // Reset the driving vars
    double forward = 0.0;
    double rotation = 0.0;
    driveSpeedCurrent = Constants.Robot.driveSpeedNormal;


    // -------------------------------------------------------------------------------------------------------
    // ELEVATOR
    // -------------------------------------------------------------------------------------------------------
    
    // if (controllerMap.isLeftDPadC1Pressed()) {
    //   elevator.gotoL2();
    // } else if (controllerMap.isUpDPadC1Pressed()) {
    //   elevator.gotoL3();
    // } else if (controllerMap.isRightDPadC1Pressed()) {
    //   elevator.gotoL4();
    // } else if (controllerMap.isDownDPadC1Pressed()) {
    //   elevator.gotoL1();
    // } else if (controllerMap.isNoDPadC1Pressed()) {
    //   elevator.hold();
    // } else if (controllerMap.isStartButtonC1Pressed()) {
    //   elevator.home();
    // }

    if (controllerMap.isUpDPadC1Pressed()) {
      elevator.manualShift(0.4);
    } else if (controllerMap.isDownDPadC1Pressed()) {
      elevator.manualShift(-0.2);
    } else if (controllerMap.isNoDPadC1Pressed()) {
      elevator.hold();
    }
    
    // -------------------------------------------------------------------------------------------------------
    // END EFFECTOR
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isRightBumperC1Pressed()) {
      // endEffector.intakeCoral();
      endEffector.manualShift(0.4);
    } else if (controllerMap.isLeftBumperC1Pressed()) {
      // endEffector.releaseCoral();
      endEffector.manualShift(-0.4);
    } else {
      endEffector.stop();
    }

    // For testing purposes, make the robot think that coral is loaded when we press the start button.
    if (controllerMap.isStartButtonC1Pressed()) {
      
    }
    // Flash lights when Coral is first loaded.
    // If on this iteration Coral is loaded, and on the last iteration Coral was not loaded, flash the LED's. 
    if (endEffector.getCoralLoaded() && wasCoralLoaded == false) {
      led.flashStatus(Constants.Led.StatusList.LOADED, 3, 0.3);
    }
    // If the lights are currently flashing, enable the override.
    if (led.getFlashing()) {
      ledFlashOverride = true;
    } else {
      ledFlashOverride = false;
    }
    // Prepare for the next iteration.
    wasCoralLoaded = endEffector.getCoralLoaded();

    // -------------------------------------------------------------------------------------------------------
    // ALGAE
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isAButtonC1Pressed()) {
      algae.manualShiftGrabber(0.6);
    } else if (controllerMap.isBButtonC1Pressed()) {
      algae.manualShiftGrabber(-0.6);
    } else {
      algae.stopGrabber();
    }

    if (controllerMap.isXButtonC1Pressed()) {
      algae.manualShiftArm(0.5);
    } else if (controllerMap.isYButtonC1Pressed()) {
      algae.manualShiftArm(-0.3);
    } else {
      algae.stopArm();
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
    
    ledBuffer = ledTeleopBuffer;
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
    led.setStatus(Constants.Led.StatusList.DISABLED);
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
