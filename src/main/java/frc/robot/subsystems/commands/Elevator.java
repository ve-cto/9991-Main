package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Encoder;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.DigitalInput;

public class Elevator {
    private WPI_VictorSPX m_elevatorLeft;
    private WPI_VictorSPX m_elevatorRight;
    private Encoder s_encoder;
    private final PIDController pid;        
    private DigitalInput s_endstop;
    private double desiredHeight;

    // You'll need to adjust these based on your elevator's specifications
    private final double COUNTS_PER_INCH = 470.0; // Example value - measure this!
    private final double GRAVITY_COMPENSATION = 0.15; // Tune this value - usually between 0.05-0.2
    
    public enum pos {
        HOME,
        L0,
        L1,
        L2,
        L3,
        UNKNOWN
    }
    private pos lastKnownPosition;
    private pos targetPosition;


    public Elevator() {
        s_encoder = new Encoder(Constants.Elevator.sEncoderID1, Constants.Elevator.sEncoderID2, false, Encoder.EncodingType.k2X);
        s_encoder.setSamplesToAverage(3);

        s_endstop = new DigitalInput(Constants.Elevator.sEndstopID);

        m_elevatorRight = new WPI_VictorSPX(Constants.Elevator.m_elevatorRightID);
        m_elevatorLeft = new WPI_VictorSPX(Constants.Elevator.m_elevatorLeftID);

        m_elevatorRight.setInverted(true);
        // m_elevatorRight.follow(m_elevatorLeft);
        
        m_elevatorLeft.setSafetyEnabled(true);
        m_elevatorRight.setSafetyEnabled(true);

        // PID values need tuning for your specific elevator
        pid = new PIDController(0.1, 0, 0);
    }

    public double getHeight() {
        return s_encoder.getDistance() / COUNTS_PER_INCH;
    }

    public double getHeightUnform() {
        return s_encoder.getDistance();
    }

    public pos getPosition() {
        return lastKnownPosition;
    }

    public String getTargetPosition() {
        try {
            return targetPosition.toString();
        } catch (NullPointerException e) {
            return "No Target";
        }
    }

    public boolean getEndstop() {
        return s_endstop.get();
    }

    public void reset() {
        s_encoder.reset();
        lastKnownPosition = pos.UNKNOWN;
        m_elevatorLeft.set(0.0);
    } 

    public void home() {
        targetPosition = pos.HOME;
        if (s_endstop.get()) {
            m_elevatorLeft.set(-0.1);
        } else {
            m_elevatorLeft.set(0.0);
            s_encoder.reset();
            lastKnownPosition = pos.HOME;
        }
        feed();
    }

    public void feed() {
        m_elevatorLeft.feed();
        m_elevatorRight.feed();
    }

    public void stop() {
        m_elevatorLeft.set(-0.1);
        m_elevatorRight.set(-0.1);
    }

    public void gotoL0() {
        if (lastKnownPosition != pos.UNKNOWN && lastKnownPosition != pos.L0) {
            setPosition(pos.L0);
            targetPosition = pos.L0;
        }
        feed();
    }

    public void gotoL1(){
        if (lastKnownPosition != pos.UNKNOWN && lastKnownPosition != pos.L1) {
            setPosition(pos.L1);
            targetPosition = pos.L1;
        }
        feed();
    }

    public void gotoL2() {
        if (lastKnownPosition != pos.UNKNOWN && lastKnownPosition != pos.L2) {
            setPosition(pos.L2);
            targetPosition = pos.L2;
        }
        feed();
    }

    public void gotoL3() {
        if (lastKnownPosition != pos.UNKNOWN && lastKnownPosition != pos.L3) {
            setPosition(pos.L3);
            targetPosition = pos.L3;
        }
        feed();
    }

    public void setPosition(pos targetHeight) {
        switch(targetHeight) {
            case HOME:
                desiredHeight = 0.1;
            case UNKNOWN:
                break;
            case L0:
                desiredHeight = 5;
            case L1:
                desiredHeight = 7;
            case L2:
                desiredHeight = 9;
            case L3:
                desiredHeight = 11;
        }

        double pidOutput = pid.calculate(getHeight(), desiredHeight);
        
        // Add gravity compensation
        // The sign is positive because we need to work against gravity
        // You might need to flip the sign depending on your motor polarity
        double motorOutput = pidOutput + GRAVITY_COMPENSATION;
        
        // Clamp the output to valid range
        motorOutput = Math.min(Math.max(motorOutput, -1.0), 1.0);
        
        m_elevatorLeft.set(-motorOutput);  
        m_elevatorRight.set(-motorOutput);
    }

    public void manualShift(double speed) {
        m_elevatorLeft.set(speed);
        m_elevatorRight.set(speed);
    }
}
