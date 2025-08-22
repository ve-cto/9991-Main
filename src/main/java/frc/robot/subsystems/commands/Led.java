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
    private int flashCount;
    private int totalFlashes;
    private double flashSpeed;
    private Constants.Led.StatusList flashStatus;
    private boolean ledOn;
    
    private AddressableLED l_led;

    private AddressableLEDBuffer l_ledBuffer;

    private Constants.Led.StatusList Status;

    private LEDPattern robotDisconnectMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.04, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(5));
    private LEDPattern robotDisconnectBase = LEDPattern.gradient(GradientType.kContinuous, Color.kDarkRed, Color.kDarkRed).atBrightness(Percent.of(50)); 
    private LEDPattern robotDisconnect = robotDisconnectBase.mask(robotDisconnectMask);
        
    private LEDPattern robotDisabledMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.3, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotDisabledBase = LEDPattern.gradient(GradientType.kContinuous, Color.kOrangeRed, Color.kDarkRed).atBrightness(Percent.of(50));
    private LEDPattern robotDisabled = robotDisabledBase.mask(robotDisabledMask);

    private LEDPattern robotIdleMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.4, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotIdleBase = LEDPattern.gradient(GradientType.kContinuous, Color.kBlue, Color.kPurple).scrollAtRelativeSpeed(Percent.per(Second).of(20)).atBrightness(Percent.of(80));
    private LEDPattern robotIdle = robotIdleBase.mask(robotIdleMask);

    private LEDPattern robotElevatorUnsafe = LEDPattern.gradient(GradientType.kContinuous, Color.kRed, Color.kDarkRed).scrollAtRelativeSpeed(Percent.per(Second).of(20)).atBrightness(Percent.of(70));

    private LEDPattern robotIdleRedMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.75, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotIdleRedBase = LEDPattern.gradient(GradientType.kContinuous, Color.kRed, Color.kPurple).scrollAtRelativeSpeed(Percent.per(Second).of(20)).atBrightness(Percent.of(80));
    private LEDPattern robotRedIdle = robotIdleBase.mask(robotIdleMask);

    private LEDPattern robotIdleBlueMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.75, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(10));
    private LEDPattern robotIdleBlueBase = LEDPattern.gradient(GradientType.kContinuous, Color.kBlue, Color.kPurple).scrollAtRelativeSpeed(Percent.per(Second).of(20)).atBrightness(Percent.of(80));
    private LEDPattern robotBlueIdle = robotIdleBase.mask(robotIdleMask);

    private LEDPattern robotAutonomousMask = LEDPattern.steps(Map.of(0, Color.kWhite, 0.6, Color.kBlack)).scrollAtRelativeSpeed(Percent.per(Second).of(50));
    private LEDPattern robotAutonomousBase = LEDPattern.rainbow(255, 200).scrollAtRelativeSpeed(Percent.per(Second).of(25));
    private LEDPattern robotAutonomous = robotAutonomousBase.mask(robotAutonomousMask);

    private LEDPattern robotReady = LEDPattern.solid(Color.kLime).breathe(Seconds.of(3)).atBrightness(Percent.of(100));

    private LEDPattern robotLoaded = LEDPattern.solid(Color.kLime).atBrightness(Percent.of(100));
    private LEDPattern robotRelease = LEDPattern.solid(Color.kOrange).atBrightness(Percent.of(100));

    private LEDPattern ledBlank = LEDPattern.solid(Color.kBlack);

    public Led() {
        l_led = new AddressableLED(Constants.Led.l_ledID);
        l_ledBuffer = new AddressableLEDBuffer(101);
        l_led.setLength(l_ledBuffer.getLength());
        l_led.setData(l_ledBuffer);
        this.isFlashing = false;
        this.flashTimer = new Timer(); // <-- important!
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
            case IDLERED:
                robotRedIdle.applyTo(this.l_ledBuffer);
                break;
            case IDLEBLUE:
                robotBlueIdle.applyTo(this.l_ledBuffer);
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
            case RELEASE:
                robotRelease.applyTo(this.l_ledBuffer);
                break;
            case BLANK:
                ledBlank.applyTo(this.l_ledBuffer);
                break;
            case UNSAFE:
                robotElevatorUnsafe.applyTo(this.l_ledBuffer);
        }

        l_led.setData(this.l_ledBuffer);
    }

    /**
     * Start flashing without blocking the main loop
     */
    public void startFlashing(Constants.Led.StatusList desiredStatus, int numFlashes, double speed) {
        // if (!isFlashing) {
        //     isFlashing = true;
        //     flashCount = 0;
        //     totalFlashes = numFlashes;
        //     flashSpeed = speed;
        //     flashStatus = desiredStatus;
        //     ledOn = false;
        //     flashTimer.reset();
        //     flashTimer.start();
        //     System.out.println("Started flashing: " + desiredStatus);
        // }
    
        // If startFlashing is retriggered, reset.
        isFlashing = true;
        flashCount = 0;
        totalFlashes = numFlashes;
        flashSpeed = speed;
        flashStatus = desiredStatus;
        ledOn = false;
        flashTimer.reset();
        flashTimer.start();
        // System.out.println("Started flashing: " + desiredStatus);
    }

    /**
     * Call this every loop in robotPeriodic or teleopPeriodic
     */
    public void updateFlashing() {
        if (isFlashing) {
            if (flashTimer.get() >= flashSpeed) {
                flashTimer.reset();
                ledOn = !ledOn;

                if (ledOn) {
                    setStatus(flashStatus);
                } else {
                    setStatus(Constants.Led.StatusList.BLANK);
                    flashCount++;
                }

                if (flashCount >= totalFlashes) {
                    isFlashing = false;
                    setStatus(Constants.Led.StatusList.BLANK);
                    // System.out.println("Flashing completed");
                }
            }
        }
    }

    public boolean getFlashing() {
        return this.isFlashing;
    }

    public void reset() {}
}



