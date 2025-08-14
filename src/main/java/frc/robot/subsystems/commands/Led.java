package frc.robot.subsystems.commands;

import static edu.wpi.first.units.Units.Percent;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

import java.util.Map;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.LEDPattern.GradientType;
import edu.wpi.first.wpilibj.util.Color;

import frc.robot.Constants;

public class Led {
    private Timer flashTimer;
    private boolean isFlashing;
    
    private AddressableLED l_led;

    private AddressableLEDBuffer l_ledBuffer;

    private Constants.Led.StatusList Status;

    private LEDPattern robotDisconnectMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.04, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(5));
    private LEDPattern robotDisconnectBase = LEDPattern.gradient(GradientType.kContinuous, Color.kDarkRed, Color.kDarkRed).atBrightness(Percent.of(50)); 
    private LEDPattern robotDisconnect = robotDisconnectBase.mask(robotDisconnectMask);
        
    private LEDPattern robotDisabledMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.1, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotDisabledBase = LEDPattern.gradient(GradientType.kContinuous, Color.kOrangeRed, Color.kDarkRed).atBrightness(Percent.of(50));
    private LEDPattern robotDisabled = robotDisabledBase.mask(robotDisabledMask);

    private LEDPattern robotIdleMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.1, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotIdleBase = LEDPattern.gradient(GradientType.kContinuous, Color.kBlue, Color.kPurple).scrollAtRelativeSpeed(Percent.per(Second).of(20)).atBrightness(Percent.of(80));
    private LEDPattern robotIdle = robotIdleBase.mask(robotIdleMask);

    private LEDPattern robotAutonomousMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.05, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(50));
    private LEDPattern robotAutonomousBase = LEDPattern.rainbow(255, 200);
    private LEDPattern robotAutonomous = robotAutonomousBase.mask(robotAutonomousMask);

    private LEDPattern robotReady = LEDPattern.solid(Color.kLime).breathe(Seconds.of(0.2)).atBrightness(Percent.of(100));
    private LEDPattern robotLoaded = LEDPattern.solid(Color.kLime).breathe(Seconds.of(0.5)).atBrightness(Percent.of(100));

    private LEDPattern ledBlank = LEDPattern.solid(Color.kBlack);

    public Led() {
        l_led = new AddressableLED(Constants.Led.l_ledID);
    
        l_ledBuffer = new AddressableLEDBuffer(150);
        
        l_led.setLength(l_ledBuffer.getLength());

        l_led.setData(l_ledBuffer);

        this.isFlashing = false;

        l_led.start();
    }

    public Constants.Led.StatusList getStatus() {
        return this.Status;
    }

    public void setAndApplyStatus(Constants.Led.StatusList desiredStatus) {
        setStatus(desiredStatus);
    }

    public void setStatus(Constants.Led.StatusList desiredStatus) {
        this.Status = desiredStatus;
        switch (desiredStatus) {
            case DISCONNECT:
                robotDisconnect.applyTo(this.l_ledBuffer);
                break;
            case DISABLED:
                robotDisabled.applyTo(this.l_ledBuffer);
                break;
            case IDLE:
                robotIdle.applyTo(this.l_ledBuffer);
                break;
            case AUTONOMOUS:
                robotAutonomous.applyTo(this.l_ledBuffer);
                break;
            case LOADED:
                robotLoaded.applyTo(this.l_ledBuffer);
                break;
            case READY:
                robotReady.applyTo(this.l_ledBuffer);
                break;
            case BLANK:
                ledBlank.applyTo(this.l_ledBuffer);
                break;
        }
        l_led.setData(this.l_ledBuffer);
        // System.out.println("LED's Status has been changed to: " + desiredStatus.toString());
    }

    public void flashStatus(Constants.Led.StatusList desiredStatus, int numFlashes, double flashSpeed) {
        if (this.isFlashing = false) {
            this.isFlashing = true;
            System.out.println("Started to Flash");
            for (int i = 0; i < numFlashes+1; i++) {
                flashTimer.restart();
                if (flashTimer.get() < flashSpeed) {
                    setStatus(desiredStatus);
                } else if (flashTimer.get() < flashSpeed * 2) {
                    setStatus(Constants.Led.StatusList.BLANK);
                } else {
                    flashTimer.restart();
                }
            }
            System.out.println("Flashing Completed");
            this.isFlashing = false;
        }
    }

    public boolean getFlashing() {
        return (this.isFlashing);
    }

    public void reset() {}
}