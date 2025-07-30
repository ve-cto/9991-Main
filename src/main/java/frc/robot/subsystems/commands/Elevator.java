package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.GroupMotorControllers;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Encoder;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.DigitalInput;

public class Elevator {
    private WPI_VictorSPX m_elevatorLeft;
    private WPI_VictorSPX m_elevatorRight;
    private Encoder s_encoder;     
    private DigitalInput s_endstop;
    private final PIDController pid;
    private double desiredHeight;

    private final double COUNTS_PER_1M = Constants.Elevator.countsPer1M;
    private final double GRAVITY_COMPENSATION = -Constants.Elevator.gravityComp;
    
    public enum Position {
        HOME,
        L1,
        L2,
        L3,
        L4,
        UNKNOWN
    }
    private Position lastKnownPosition;
    private Position targetPosition;

    public Elevator() {
        s_encoder = new Encoder(Constants.Elevator.sEncoderID1, Constants.Elevator.sEncoderID2, false, Encoder.EncodingType.k2X);
        s_encoder.setSamplesToAverage(3);

        s_endstop = new DigitalInput(Constants.Elevator.sEndstopID);

        m_elevatorRight = new WPI_VictorSPX(Constants.Elevator.m_elevatorRightID);
        m_elevatorLeft = new WPI_VictorSPX(Constants.Elevator.m_elevatorLeftID);

        m_elevatorRight.setInverted(true);
        m_elevatorRight.follow(m_elevatorLeft);
        
        m_elevatorLeft.setSafetyEnabled(true);
        m_elevatorRight.setSafetyEnabled(true);


        // PID values
        // kP controls how quickly motor operands are set, when the heading isn't the desired value.
        // kI 
        // kD gives the controller an extra "nudge", in case it never reaches it's target value.
        pid = new PIDController(1, 0.9, 0.0);
    }

    // -----------------------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------------------
    
    /*
     * Get the height of the elevator, in steps of 10 centimeters
     */
    public double getHeight() {
        return s_encoder.getDistance() / COUNTS_PER_1M;
    }

    /*
     * Get the unformatted height of the elevator
     */
    public double getHeightRaw() {
        return s_encoder.getDistance();
    }

    /*
     * Get the last known position of the elevator
     */
    public Position getPosition() {
        return lastKnownPosition;
    }

    /*
     * Get the position that the elevator is currently attempting to range towards
     */
    public String getTargetPosition() {
        try {
            return targetPosition.toString();
        } catch (NullPointerException e) {
            return "No Target";
        }
    }

    /*
     * Get the status of the homing endstop
     */
    public boolean getEndstop() {
        return s_endstop.get();
    }

    // -----------------------------------------------------------------------
    // SETTERS
    // -----------------------------------------------------------------------
    
    /*
     * ONLY TO BE USED IF IN ZERO POSITION (HOMED)
     * Reset the encoder.
     */
    public void reset() {
        s_encoder.reset();
        // lastKnownPosition = Position.UNKNOWN;
        lastKnownPosition = Position.HOME;
    }

    // -----------------------------------------------------------------------
    // MOVE
    // -----------------------------------------------------------------------
    
    /*
     * Return to home position, and reset the encoder once reached.
     */
    public void home() {
        targetPosition = Position.HOME;
        if (!s_endstop.get() && getHeight() > 3.1) {
            setPosition(targetPosition);
        
        } else if (!s_endstop.get() && getHeight() < 3.1) {
            manualShift(0.05);
        
        } else {
            m_elevatorLeft.set(0.0);
            m_elevatorRight.set(0.0);
            reset();
        }
        feed();
    }

    /*
     * Range to the coral trough. (First position)
     */
    public void gotoL1() {
        if (lastKnownPosition != Position.UNKNOWN) {
            setPosition(Position.L1);
            targetPosition = Position.L1;
        }
        feed();
    }

    /*
     * Range to the lowest tree level (Second position)
     */
    public void gotoL2(){
        if (lastKnownPosition != Position.UNKNOWN) {
            setPosition(Position.L2);
            targetPosition = Position.L2;
        }
        feed();
    }

    /*
     * Range to the middle tree level (Third position)
     */
    public void gotoL3() {
        if (lastKnownPosition != Position.UNKNOWN) {
            setPosition(Position.L3);
            targetPosition = Position.L3;
        }
        feed();
    }

    /*
     * Range to the highest scoring position (Fourth position)
     */
    public void gotoL4() {
        if (lastKnownPosition != Position.UNKNOWN) {
            setPosition(Position.L4);
            targetPosition = Position.L4;
        }
        feed();
    }

    /*
     * Move the elevator to a height corresponding to each coral level.
     * Automatically applies acceleration graphs.
     * targetHeight must be type of enumerator Position.
     */
    public void setPosition(Position targetHeight) {
        if (targetHeight == Position.HOME) {
            desiredHeight = 0;
        }
        else if (targetHeight == Position.UNKNOWN) {
            desiredHeight = 0.6;
        }
        else if (targetHeight == Position.L1) {
            desiredHeight = 0.06;
        }
        else if (targetHeight == Position.L2) {
            desiredHeight = 0.135;
        }
        else if (targetHeight == Position.L3) {
            desiredHeight = 0.51;
        }
        else if (targetHeight == Position.L4) {
            desiredHeight = 1.1;
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

    /*
     * Shift the elevator manually.
     * Positive speed means up, negative means down.
     */
    public void manualShift(double speed) {
        m_elevatorLeft.set(-speed);
        m_elevatorRight.set(-speed);
    }

    /*
     * Hold the current elevator position, using gravity compensation.
     */
    public void hold() {
        m_elevatorLeft.set(GRAVITY_COMPENSATION);
        m_elevatorRight.set(GRAVITY_COMPENSATION);
    }

    /*
     * Feed the motors, avoiding motorsafety triggers.
     */
    public void feed() {
        m_elevatorLeft.feed();
        m_elevatorRight.feed();
    }
}
