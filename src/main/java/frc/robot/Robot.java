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
import frc.robot.subsystems.commands.Limelight;
// import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj2.command.button.JoystickButton;

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
  private Limelight limelight;
  
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
  private static final String autoCustom2 = "Custom2";
  private static final String autoCustom3 = "Custom3";
  private static final String autoCustom4 = "Custom4";
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
    limelight = new Limelight();

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

    autoChooser.setDefaultOption("Score on L3", autoDefault);
    autoChooser.addOption("Drive off Starting Line", autoCustom1);
    autoChooser.addOption("Score on L2", autoCustom2);
    autoChooser.addOption("Score on L4", autoCustom3);
    autoChooser.addOption("SIDE Score on L4", autoCustom4);
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

    limelight.periodic();

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

    autoTimer.reset();
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
      // Place a coral on L3
      case autoDefault:
        algae.stopArm();
        algae.stopGrabber();

        if (t < 3) {
          autoState = "Driving up to Coral Tree";

          endEffector.stop();
          elevator.home();
          driveSubsystem.drive(-0.55, limelight.getAimMotorOutput(0.0), 1.0);

        } else if (t < 5) {
          autoState = "Raising Elevator to L3";

          endEffector.stop();
          driveSubsystem.stop();
          elevator.gotoL3();

        } else if (t < 6) {
          autoState = "Releasing Coral on L3";

          elevator.gotoL3();
          driveSubsystem.stop();
          endEffector.manualShift(0.7);
          endEffector.debugState(0);

        } else if (t < 8 ) {
          autoState = "Lowering Elevator";

          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();

        } else {
          // Finished
          autoState = "Finished";

          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
        }
        break;

      // Drive off starting line, but do nothing else.
      case autoCustom1:
        endEffector.stop();
        elevator.home();
        algae.stopArm();
        algae.stopGrabber();
        if (t < 2) {
          driveSubsystem.drive(-0.5, 0.0, 1.0);
        } else {
          driveSubsystem.stop();
        }
        break;


      case autoCustom2:
        algae.stopArm();
        algae.stopGrabber();

        if (t < 3) {
          autoState = "Driving up to Coral Tree";

          endEffector.stop();
          elevator.home();
          driveSubsystem.drive(-0.5, 0.0, 1.0);
        
        } else if (t < 5) {
          autoState = "Raising Elevator to L2";
        
          endEffector.stop();
          driveSubsystem.stop();
          elevator.gotoL2();
        
        } else if (t < 6) {
          autoState = "Releasing Coral on L2";
        
          elevator.gotoL2();
          driveSubsystem.stop();
          endEffector.manualShift(0.7);
          endEffector.debugState(0);
        
        } else if (t < 8 ) {
          autoState = "Lowering Elevator";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
        } else {
          // Finished
          autoState = "Finished";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
        }
        break;


      case autoCustom3:
        algae.stopArm();
        algae.stopGrabber();

        if (t < 3) {
          autoState = "Driving up to Coral Tree";

          endEffector.stop();
          elevator.home();
          driveSubsystem.drive(-0.55, limelight.getAimMotorOutput(0.0), 1.0);
        
        } else if (t < 8) {
          autoState = "Raising Elevator to L4";
        
          endEffector.stop();
          driveSubsystem.drive(-0.4, limelight.getAimMotorOutput(0.0), 1.0);
          elevator.gotoL4();
        
        } else if (t < 9) {
          autoState = "Releasing Coral on L4";
        
          elevator.gotoL4();
          driveSubsystem.stop();
          endEffector.releaseCoral();
        
        } else if (t < 11) {
          autoState = "Lowering Elevator";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
        } else  if (t <= 15) {
          // Finished
          autoState = "Finished";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
          autoTimer.stop();
        }
        break;


      // case autoCustom4:
      //   if (t < 2) {
      //     autoState = "Driving close to Coral Tree";

      //     endEffector.stop();
      //     elevator.home();
      //     algae.stopGrabber();
      //     driveSubsystem.drive(-0.54, 0.0, 1.0);
      //   } else if (t < 5) {
      //     autoState = "Lifting Arm";

      //     endEffector.stop();
      //     driveSubsystem.stop();
      //     algae.stopGrabber();
      //     algae.manualShiftArm(0.4);
      //     elevator.gotoAlgaeBottom();
      //   } else if (t < 6) {
      //     autoState = "Driving up to Coral Tree";

      //     elevator.gotoAlgaeBottom();
      //     algae.stopArm();
      //     algae.manualShiftGrabber(-0.4);
      //     driveSubsystem.drive(-0.7, 0.0, 1.0);
      //   } else if (t < 7) {
      //     autoState = "Grabbing Algae";

      //     elevator.gotoAlgaeBottom();
      //     algae.manualShiftGrabber(-0.6);
      //     algae.manualShiftArm(-0.05);
      //     driveSubsystem.stop();
      //   } else if (t < 7.5) {
      //     algae.manualShiftGrabber(-0.6);
      //     algae.stopArm();
      //     driveSubsystem.drive(0.2, 0.0, 0.0);
      //   } else if (t < 8) {
      //     autoState = "Algae Released, placing Coral";

      //     driveSubsystem.drive(-0.1, 0.4, 1.0);
      //     algae.stopGrabber();
      //   } else if (t < 10) {
      //     autoState = "Raising Elevator";

      //     elevator.gotoL3();
      //     algae.manualShiftArm(-0.6);
      //   } else if (t < 12) {
      //     autoState = "Re-aligning...";
          
      //     driveSubsystem.drive(-0.6, 0.0, 1.0);
      //   } else if (t < 13) {
          
      //     driveSubsystem.stop();
      //     endEffector.releaseCoral();
      //   } else if (t < 15) {
      //     endEffector.stop();
      //     elevator.home();
      //     algae.stopArm();
      //   }

      //   break;
        

      case autoCustom4:
        algae.stopArm();
        algae.stopGrabber();

        if (t < 3) {
          autoState = "Driving up to Coral Tree";

          endEffector.stop();
          elevator.home();
          driveSubsystem.drive(-0.65, limelight.getAimMotorOutput(0.0), 1.0);
        
        } else if (t < 8) {
          autoState = "Raising Elevator to L4";
        
          endEffector.stop();
          driveSubsystem.drive(-0.47, limelight.getAimMotorOutput(0.0), 1.0);
          elevator.gotoL4();
        
        } else if (t < 9) {
          autoState = "Releasing Coral on L4";
        
          elevator.gotoL4();
          driveSubsystem.drive(-0.3, 0.0, 1.0);
          endEffector.releaseCoral();
        
        } else if (t < 11) {
          autoState = "Lowering Elevator";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
        } else  if (t <= 15) {
          // Finished
          autoState = "Finished";
        
          driveSubsystem.stop();
          elevator.home();
          endEffector.stop();
          autoTimer.stop();
        }
        break;


      // if none of the above auto's gets triggered (something goes wrong), stop everything
      default:
        endEffector.stop();
        driveSubsystem.stop();
        elevator.hold();
        algae.stopArm();
        algae.stopGrabber();
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

  // @SuppressWarnings("unlikely-arg-type")
  @Override
  public void teleopPeriodic() {
    // System.out.println(DriverStation.getAlliance().toString());
    
    // if (DriverStation.getAlliance().toString() == "Optional[Red]") {
    //   ledTeleopBuffer = Constants.Led.StatusList.IDLERED;
    // } else if (DriverStation.getAlliance().toString() == "Optional[Blue]") {
    //   ledTeleopBuffer = Constants.Led.StatusList.IDLEBLUE;
    // }


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
      elevator.home();
    // } else if (controllerMap.isRightStickButtonC1Pressed()) {
    //   elevator.gotoAlgaeTop();
    // } else if (controllerMap.isLeftStickButtonC1Pressed()) {
    //   elevator.gotoAlgaeBottom();
    // } 
    } else if (controllerMap.isNoDPadC1Pressed()) {
      elevator.hold();
    }

    // else if (controllerMap.isStartButtonC1Pressed()) {
    //   elevator.manualShift(-0.6);
    // }
    // else if (controllerMap.isStartButtonC1Pressed()) {
    //   elevator.home();
    // }

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
    } else if (controllerMap.isLeftBumperC1Pressed()) {
      endEffector.intakeCoral();
    } else {
      endEffector.stop();
    }

    // -------------------------------------------------------------------------------------------------------
    // ALGAE
    // -------------------------------------------------------------------------------------------------------
    if (controllerMap.isAButtonC1Pressed()) {
      algae.manualShiftGrabber(0.4);
    } else if (controllerMap.isBButtonC1Pressed()) {
      algae.manualShiftGrabber(-0.4);
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
    
    // Reset the driving vars
    double forward = 0.0;
    double rotation = 0.0;
    driveSpeedCurrent = Constants.Drive.driveSpeedNormal;
    
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
    // if (elevator.getPosition().equals(Constants.Elevator.Position.HOME) || elevator.getPosition().equals(Constants.Elevator.Position.L1) || elevator.getPosition().equals(Constants.Elevator.Position.L2)) {
    //   driveSpeedCurrent = Constants.Drive.driveSpeedNormal;
    // } else {
    //   driveSpeedCurrent = Constants.Drive.driveSpeedElevator;
    // }

    // Arcadedrive the robot using the selected drive scheme
    if (driveSchemeSelected == 0) {
      // Single Controller
      forward = controllerMap.getLeftYC1();
      rotation = controllerMap.getRightXC1();

      if (controllerMap.isLeftTriggerC1Pressed() || controllerMap.isRightTriggerC1Pressed()) {
        driveSpeedCurrent = Constants.Drive.driveSpeedFast;
      } else if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
        driveSpeedCurrent = Constants.Drive.driveSpeedFaster;
      }
    } else if (driveSchemeSelected == 1) {
      // Two Controller
      forward = controllerMap.getLeftYC2();
      rotation = controllerMap.getRightXC2();

      if (controllerMap.isLeftTriggerC1Pressed() && controllerMap.isRightTriggerC1Pressed()) {
        endEffector.manualShift(-0.8);
        endEffector.debugState(0);
      }

      if (controllerMap.isLeftTriggerC2Pressed() || controllerMap.isRightTriggerC2Pressed()) {
        driveSpeedCurrent = Constants.Drive.driveSpeedFast;
      } else if (controllerMap.isLeftTriggerC2Pressed() && controllerMap.isRightTriggerC2Pressed()) {
        driveSpeedCurrent = Constants.Drive.driveSpeedFaster;
      }
    } else {
      forward = 0;
      rotation = 0;
      System.out.print("Strangely, a drive scheme could not be selected, and an error occured.");
    }
    
    rotation = rotation * Constants.Drive.turnMultiplier;
    forward = Math.min(Math.max(forward, -1.0), 1.0);
    rotation = Math.min(Math.max(rotation, -1.0), 1.0);

    if (controllerMap.isRightStickButtonC1Pressed() && controllerMap.isLeftStickButtonC1Pressed()) {
      forward = limelight.getRangeMotorOutput(0.0);
      rotation = limelight.getAimMotorOutput(0.0);
    } else if (controllerMap.isRightStickButtonC1Pressed()) {
      rotation = limelight.getAimMotorOutput(0.0);
    } else if (controllerMap.isLeftStickButtonC1Pressed()) {
      forward = limelight.getRangeMotorOutput(0.2);
    }

    driveSubsystem.drive(forward, rotation, driveSpeedCurrent); 

    //
    // LED's
    //
    ledTeleopBuffer = Constants.Led.StatusList.IDLE;
    if (!elevator.getEndstop()) {
      ledTeleopBuffer = Constants.Led.StatusList.UNSAFE;
    }
    // If Coral's loaded, set LED's to ready. (Does not overwrite flashes)
    if (endEffector.getCoralLoaded()) {
      ledTeleopBuffer = Constants.Led.StatusList.READY;
    }
    // Set the global buffer for LED's
    ledBuffer = ledTeleopBuffer;

    networkDriveForward.set(forward);
    networkDriveRotation.set(rotation);
    networkDriveSpeed.set(driveSpeedCurrent);
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
