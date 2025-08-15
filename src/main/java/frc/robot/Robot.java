package frc.robot;

import java.security.Key;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import frc.robot.subsystems.tools.MapRanges;
import frc.robot.subsystems.commands.Elevator;
import frc.robot.subsystems.commands.EndEffector;
import frc.robot.subsystems.commands.Algae;
import frc.robot.subsystems.commands.Led;

// import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the manifest file in the resource directory.
 */
public class Robot extends TimedRobot {
  private ControllerMap controllerMap;
  private DriveSubsystem driveSubsystem;
  private MapRanges mapRanges;
  private Elevator elevator;
  private EndEffector endEffector;
  private Algae algae;
  private Led led;
  private Constants.Led.StatusList ledBuffer;
  private Constants.Led.StatusList ledTeleopBuffer;
  
  private Timer autoTimer;
  private String autoState;

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

  private static final Boolean coralLoadedAuto = true;
  private static final Boolean coralUnloadedAuto = false;
  private final SendableChooser<Boolean> autoCoralChooser = new SendableChooser<>();

  private final NetworkTableInstance networkTableInstance = NetworkTableInstance.getDefault();
    
  private final NetworkTable driveTable = networkTableInstance.getTable("Drive");
  private final NetworkTable elevatorTable =  networkTableInstance.getTable("Elevator");
  private final NetworkTable endEffectorTable = networkTableInstance.getTable("EndEffector");
  private final NetworkTable limelightTable = networkTableInstance.getTable("Limelight");
  private final NetworkTable ledTable = networkTableInstance.getTable("LED's");
  private final NetworkTable autonomousTable = networkTableInstance.getTable("Autonomous");

  DoublePublisher networkDriveForward;
  StringPublisher networkElevatorRange;
  BooleanPublisher networkElevatorEndstop;
  StringPublisher networkEndEffectorStatus;
  BooleanPublisher networkEndEffectorCoral;
  StringPublisher networkLEDStatus;
  BooleanPublisher networkLEDFlashing;
  StringPublisher networkAutoRunning;
  StringPublisher networkAutoState;
  DoublePublisher networkDriveRotation;
  DoublePublisher networkDriveSpeed;
  StringPublisher networkElevatorPos;
  DoublePublisher networkElevatorRHeight;
  DoublePublisher networkElevatorHeight;
  BooleanPublisher networkEndEffectorLaser;

  public Robot() {}

  @Override
  public void robotInit() {
    // Create an instance of controllerMap and driveSubsystem
    controllerMap = new ControllerMap();
    driveSubsystem = new DriveSubsystem();
    mapRanges = new MapRanges();
    elevator = new Elevator();
    endEffector = new EndEffector();
    algae = new Algae();
    led = new Led();

    autoTimer = new Timer();
    
    // set preventDrive to false on init
    this.preventDrive = false;
  
    networkDriveForward = driveTable.getDoubleTopic("Drive Forward Value").publish();
    networkDriveRotation = driveTable.getDoubleTopic("Drive Rotation Value").publish();
    networkDriveSpeed = driveTable.getDoubleTopic("Drive Speed").publish();
    

    networkElevatorPos = elevatorTable.getStringTopic("Elevator Position").publish();
    networkElevatorRHeight = elevatorTable.getDoubleTopic("Elevator Raw Height").publish();
    networkElevatorHeight = elevatorTable.getDoubleTopic("Elevator Height").publish();
    networkElevatorRange = elevatorTable.getStringTopic("Elevator Ranging Towards").publish();
    networkElevatorEndstop = elevatorTable.getBooleanTopic("Elevator Endstop").publish();
    
    networkEndEffectorStatus = endEffectorTable.getStringTopic("Intake Status").publish();
    networkEndEffectorCoral = endEffectorTable.getBooleanTopic("Coral Loaded?").publish();
    networkEndEffectorLaser = endEffectorTable.getBooleanTopic("Endstop Status").publish();

    networkLEDStatus = ledTable.getStringTopic("LED Status").publish();
    networkLEDFlashing = ledTable.getBooleanTopic("LED's Flashing?").publish();

    networkAutoState = autonomousTable.getStringTopic("Current Auto Action").publish();

    SmartDashboard.getBoolean("Prevent Driver Control?", preventDrive);
    SmartDashboard.getBoolean("Use Joysticks to Drive?", useJoystickDrive);

    autoChooser.setDefaultOption("Default Auto", autoDefault);
    autoChooser.addOption("Custom 1", autoCustom1);
    SmartDashboard.putData("Pick an auto to run in Autonomous.", autoChooser);

    driveSchemeChooser.setDefaultOption("Single-Controller Control", driveSchemeDefault);
    driveSchemeChooser.addOption("Dual-Controller Control", driveSchemeDual);
    driveSchemeChooser.addOption("Joystick Control", driveSchemeJoystick);
    SmartDashboard.putData("How to control the robot?", driveSchemeChooser);

    autoCoralChooser.setDefaultOption("Yes", coralLoadedAuto);
    autoCoralChooser.setDefaultOption("No", coralUnloadedAuto);
    SmartDashboard.putData("Is coral loaded in the robot for Autonomous?", autoCoralChooser);

    elevator.reset();
    ledBuffer = Constants.Led.StatusList.BLANK; 
    CameraServer.startAutomaticCapture();
  }

  @Override
  public void robotPeriodic() {
    // // For testing purposes, make the robot think the coral is loaded when we press a button.
    // if (controllerMap.isJoystickButtonPressed(1)) {
    //   endEffector.debugState(0);
    // } else if (controllerMap.isJoystickButtonPressed(2)) {
    //   endEffector.debugState(1);
    // } else if (controllerMap.isJoystickButtonPressed(3)) {
    //   endEffector.debugState(2);
    // }

    // Flash lights when Coral is first loaded.
    // If on this iteration Coral is loaded, and on the last iteration Coral was not loaded, flash the LED's. 
    if (endEffector.getCoralLoaded() && wasCoralLoaded == false) {
      led.startFlashing(Constants.Led.StatusList.LOADED, 3, 0.1);
    }
    // If on this iteration Coral is NOT loaded, and on the last iteration WAS loaded, flash.
    if (!endEffector.getCoralLoaded() && wasCoralLoaded == true) {
      led.startFlashing(Constants.Led.StatusList.RELEASE, 3, 0.1);
    }

    // Prepare for the next iteration.
    wasCoralLoaded = endEffector.getCoralLoaded();

    // If the LED's aren't flashing, set the buffer. If they are, then seperate logic takes over.
    if (led.getFlashing()) {
      led.updateFlashing();    
    } else {
      led.setStatus(ledBuffer);
    }

    networkDriveForward.set(forward);
    networkDriveRotation.set(rotation);
    networkDriveSpeed.set(driveSpeedCurrent);
    

    networkElevatorPos.set(elevator.getPosition().toString());
    networkElevatorRHeight.set(elevator.getHeightRaw());
    networkElevatorHeight.set(elevator.getHeight());
    networkElevatorRange.set(elevator.getTargetPosition());
    networkElevatorEndstop.set(elevator.getEndstop());
    
    networkEndEffectorStatus.set(endEffector.getCoralState());
    networkEndEffectorCoral.set(endEffector.getCoralLoaded());
    networkEndEffectorLaser.set(endEffector.getEndstop());

    networkLEDStatus.set(led.getStatus().toString());
    networkLEDFlashing.set(led.getFlashing());
  }

  @Override
  public void autonomousInit() {
    autoSelected = autoChooser.getSelected();

    autoTimer.start();
    autoTimer.restart();
    (autonomousTable.getStringTopic("Running Auto").publish()).set(autoSelected);

    if (autoCoralChooser.getSelected()) {
      endEffector.debugState(2);
    }
  }

  public void autonomousPeriodic() {
    ledBuffer = Constants.Led.StatusList.AUTONOMOUS;
    autoState = "Idle";
    double t = autoTimer.get();

    switch (autoSelected) {
      case autoCustom1:
        // Custom auto here (keep calling subsystem methods every loop to feed MotorSafety)
        break;
  
      case autoDefault: {
        algae.stopArm();
        algae.stopGrabber();
        elevator.hold();
  
        if (t < 5.0) {
          // 0–5s: intake
          autoState = "Intaking";
          endEffector.intakeCoral();
          // Feed drivetrain even when stationary
          driveSubsystem.drive(0.0, 0.0, 1.0);
        } else if (t < 7.0) {
          // 5–7s: drive forward at 50%
          autoState = "Driving";
          endEffector.stop();
          driveSubsystem.drive(0.5, 0.0, 1.0); // 50% forward, no turn
        } else if (t < 9.0) {
          // 7–9s: release
          autoState = "Releasing";
          endEffector.releaseCoral();
          // Hold position and keep feeding drivetrain
          driveSubsystem.drive(0.0, 0.0, 1.0);
        } else {
          // >9s: stop everything, keep feeding
          autoState = "Stopped";
          endEffector.stop();
          driveSubsystem.drive(0.0, 0.0, 1.0);
        }
        break;
      }
  
      default:
        // Safety fallback: stop and feed
        endEffector.stop();
        driveSubsystem.drive(0.0, 0.0, 1.0);
        break;
      }
      networkAutoState.set(autoState);
  }

  @Override
  public void teleopInit() {
    driveSchemeSelected = driveSchemeChooser.getSelected();
    (driveTable.getStringTopic("Drive Scheme").publish()).set(DRIVE_SCHEME_STRINGS[driveSchemeSelected]);
    elevator.reset();
  }

  @SuppressWarnings("unlikely-arg-type")
  @Override
  public void teleopPeriodic() {
    ledTeleopBuffer = Constants.Led.StatusList.IDLE;

    // System.out.println(DriverStation.getAlliance().toString());
    
    // if (DriverStation.getAlliance().toString() == "Optional[Red]") {
    //   ledTeleopBuffer = Constants.Led.StatusList.IDLERED;
    // } else if (DriverStation.getAlliance().toString() == "Optional[Blue]") {
    //   ledTeleopBuffer = Constants.Led.StatusList.IDLEBLUE;
    // }

    // Reset the driving vars
    double forward = 0.0;
    double rotation = 0.0;
    driveSpeedCurrent = Constants.Drive.driveSpeedNormal;


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
      elevator.manualShift(-0.25);
    } else if (controllerMap.isNoDPadC1Pressed()) {
      elevator.hold();
    }

    // if (controllerMap.isUpDPadC1Pressed()) {
    //   elevator.manualShift(0.5);
    // } else if (controllerMap.isDownDPadC1Pressed()) {
    //   elevator.manualShift(-0.25);
    // } else if (controllerMap.isNoDPadC1Pressed()) {
    //   elevator.hold();
    // }
    
    // -------------------------------------------------------------------------------------------------------
    // END EFFECTOR
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isRightBumperC1Pressed()) {
      endEffector.releaseCoral();
      // endEffector.manualShift(0.4);
    } else if (controllerMap.isLeftBumperC1Pressed()) {
      endEffector.intakeCoral();
      // endEffector.manualShift(-0.4);
    } else {
      endEffector.stop();
    }

    // If Coral's loaded, set LED's to ready. (Does not overwrite flashes)
    if (endEffector.getCoralLoaded()) {
      ledTeleopBuffer = Constants.Led.StatusList.READY;
    }

    // -------------------------------------------------------------------------------------------------------
    // ALGAE
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isAButtonC1Pressed()) {
      algae.manualShiftGrabber(0.5);
    } else if (controllerMap.isBButtonC1Pressed()) {
      algae.manualShiftGrabber(-0.5);
    } else {
      algae.stopGrabber();
    }

    if (controllerMap.isYButtonC1Pressed()) {
      algae.manualShiftArm(0.5);
    } else if (controllerMap.isXButtonC1Pressed()) {
      algae.manualShiftArm(-0.3);
    } else {
      algae.stopArm();
    }

    // -------------------------------------------------------------------------------------------------------
    // DRIVE
    // -------------------------------------------------------------------------------------------------------
    // // Handle Triggers for Drive Speed
    // if (controllerMap.isLeftTriggerC1Pressed() && !controllerMap.isRightTriggerC1Pressed()) {
    //   driveSpeedCurrent = Constants.Robot.driveSpeedSlow;
    // } else if (controllerMap.isRightTriggerC1Pressed() && !controllerMap.isLeftTriggerC1Pressed()) {
    //   driveSpeedCurrent = Constants.Robot.driveSpeedFast;
    // } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
    //   // If both of the triggers are held at the same time, max the motors.
    //   driveSpeedCurrent = Constants.Robot.driveSpeedMax;
    // }

    // Change the drive speed based on elevator position
    // if (elevator.getPosition().equals(Constants.Elevator.Position.HOME)) {
    //   driveSpeedCurrent = Constants.Drive.driveSpeedNormal;
    // } else if (elevator.getPosition().equals(Constants.Elevator.Position.L1)) {
    //   driveSpeedCurrent = Constants.Drive.driveSpeedL1;
    // } else if (elevator.getPosition().equals(Constants.Elevator.Position.L2) || elevator.getPosition().equals(Constants.Elevator.Position.L3) || elevator.getPosition().equals(Constants.Elevator.Position.L4)) {
    //   driveSpeedCurrent = Constants.Drive.driveSpeedElevator;
    // }

    // If the driver is speeding up the robot, acknowledge their request, and bypass whatever the elevator is doing
    if ((controllerMap.isLeftTriggerC1Pressed() && !controllerMap.isRightTriggerC1Pressed()) || (!controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed())) {
      driveSpeedCurrent = Constants.Drive.driveSpeedFast;
    } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
      driveSpeedCurrent = Constants.Drive.driveSpeedFaster;
    }

    // Arcadedrive the robot using the selected drive scheme
    if (driveSchemeSelected == 0) {
      // Single Controller
      forward = controllerMap.getRightXC1();
      rotation = controllerMap.getLeftYC1();
    } else if (driveSchemeSelected == 1) {
      // Two Controller
      forward = controllerMap.getRightXC2();
      rotation = -controllerMap.getLeftYC2();
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

    this.forward = forward * Constants.Drive.turnMultiplier;
    this.rotation = rotation;
    this.forward = Math.min(Math.max(this.forward, -1.0), 1.0);
    this.rotation = Math.min(Math.max(this.rotation, -1.0), 1.0);
    driveSubsystem.drive(this.forward, this.rotation, driveSpeedCurrent); 
    
    // Set the global buffer for LED's
    ledBuffer = ledTeleopBuffer;
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {
    if (!DriverStation.isDSAttached()) {
      ledBuffer = Constants.Led.StatusList.DISCONNECT;
    } else if (DriverStation.isDSAttached()) {
      ledBuffer = Constants.Led.StatusList.DISABLED;
    }
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
