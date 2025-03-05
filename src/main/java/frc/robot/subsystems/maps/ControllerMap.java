// ControllerMap.java
package frc.robot.subsystems.maps;

import java.text.DecimalFormat;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

public class ControllerMap {
    private XboxController controller1;
    private XboxController controller2;
    private Joystick joystick;
    private final double triggerThreshold = 0.5;
    private double axisValue;


    public ControllerMap() {
        controller1 = new XboxController(0);
        controller2 = new XboxController(2);
        joystick = new Joystick(1);
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Methods to check if the buttons on controller 1 are pressed
    public boolean isAButtonC1Pressed() {
        return controller1.getAButton();
    }

    public boolean isBButtonC1Pressed() {
        return controller1.getBButton();
    }

    public boolean isXButtonC1Pressed() {
        return controller1.getXButton();
    }

    public boolean isYButtonC1Pressed() {
        return controller1.getYButton();
    }

    public boolean isLeftBumperC1Pressed() {
        return controller1.getLeftBumperButton();
    }

    public boolean isRightBumperC1Pressed() {
        return controller1.getRightBumperButton();
    }

    public boolean isBackButtonC1Pressed() {
        return controller1.getBackButton();
    }

    public boolean isStartButtonC1Pressed() {
        return controller1.getStartButton();
    }

    public boolean isLeftStickButtonC1Pressed() {
        return controller1.getLeftStickButton();
    }

    public boolean isRightStickButtonC1Pressed() {
        return controller1.getRightStickButton();
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Methods to check if the buttons on controller 2 are pressed
    public boolean isAButtonC2Pressed() {
        return controller2.getAButton();
    }

    public boolean isBButtonC2Pressed() {
        return controller2.getBButton();
    }

    public boolean isXButtonC2Pressed() {
        return controller2.getXButton();
    }

    public boolean isYButtonC2Pressed() {
        return controller2.getYButton();
    }

    public boolean isLeftBumperC2Pressed() {
        return controller2.getLeftBumperButton();
    }

    public boolean isRightBumperC2Pressed() {
        return controller2.getRightBumperButton();
    }

    public boolean isBackButtonC2Pressed() {
        return controller2.getBackButton();
    }

    public boolean isStartButtonC2Pressed() {
        return controller2.getStartButton();
    }

    public boolean isLeftStickButtonC2Pressed() {
        return controller2.getLeftStickButton();
    }

    public boolean isRightStickButtonC2Pressed() {
        return controller2.getRightStickButton();
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Methods to get the left stick Y and X axis values for controller 1
    public double getLeftYC1() {
        return controller1.getLeftY();
    }

    public double getLeftXC1() {
        return controller1.getLeftX();
    }

    // Methods to get the right stick Y and X axis values for controller 1
    public double getRightYC1() {
        return controller1.getRightY();
    }

    public double getRightXC1() {
        return controller1.getRightX();
    }

    // Methods to get the left stick Y and X axis values for controller 2
    public double getLeftYC2() {
        return controller2.getLeftY();
    }

    public double getLeftXC2() {
        return controller2.getLeftX();
    }

    // Methods to get the right stick Y and X axis values for controller 2
    public double getRightYC2() {
        return controller2.getRightY();
    }

    public double getRightXC2() {
        return controller2.getRightX();
    }
    
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    public boolean isLeftTriggerC1Pressed() {
        return controller1.getLeftTriggerAxis() > triggerThreshold;
    }

    public boolean isRightTriggerC1Pressed() {
        return controller1.getRightTriggerAxis() > triggerThreshold;
    }
    
    public boolean isLeftTriggerC2Pressed() {
        return controller2.getLeftTriggerAxis() > triggerThreshold;
    }
    
    public boolean isRightTriggerC2Pressed() {
        return controller2.getRightTriggerAxis() > triggerThreshold;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public boolean isJoystickButtonPressed(int buttonNumber) {
        return joystick.getRawButton(buttonNumber);
    }

    public double getJoystickAxes(int axisNumber) {
        axisValue = joystick.getRawAxis(axisNumber);
        return axisValue;
    }

    public double getAxisCount() {
        return joystick.getAxisCount();
    }

}
