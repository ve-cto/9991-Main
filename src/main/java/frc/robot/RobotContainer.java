package frc.robot;

import frc.robot.subsystems.commands.DriveSubsystem;
import frc.robot.subsystems.maps.ControllerMap;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class RobotContainer {
    private final DriveSubsystem driveSubsystem = new DriveSubsystem();
    private final ControllerMap controllers = new ControllerMap();
    private final MotorControlSubsystem motorControl = new MotorControlSubsystem();
    
    private final Command driveMax = Commands.runOnce(() -> driveSubsystem.setSpeed(1.0));
    private final Command driveSlow = Commands.runOnce(() -> driveSubsystem.setSpeed(0.5));
    private final Command driveNormal = Commands.runOnce(() -> driveSubsystem.setSpeed(0.7));
    private final Command driveFast = Commands.runOnce(() -> driveSubsystem.setSpeed(0.8));

    private final Command spinUpShooter = Commands.runOnce(() -> motorControl.SpinUpShooter());
    private final Command shootNote = Commands.runOnce(() -> motorControl.ShootNote());

    private final Command intakeNote = Commands.runOnce(() -> motorControl.IntakeNote());
    private final Command ejectNote = Commands.runOnce(() -> motorControl.EjectNote());
    
    // private final Command spinUpShooter = Commands.runOnce -> 


    public RobotContainer() {
        configureButtons();
    }

    private void configureButtons() {
        // controllers.aButtonC1.onTrue();
    }
}
