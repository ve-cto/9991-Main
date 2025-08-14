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
        pid = new PIDController(Constants.Elevator.kp, Constants.Elevator.ki, Constants.Elevator.kd);
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
     * (Inverted because it's silly)
     */
    public boolean getEndstop() {
        return !s_endstop.get();
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
        if (!s_endstop.get() && getHeight() > 0.2) {
            setPosition(targetPosition);
        
        } else if (!s_endstop.get() && getHeight() < 0.2) {
            manualShift(-0.1);
        
        } else {
            manualShift(0.0);
            reset();
        }
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
            desiredHeight = 0.1;
        } else if (targetHeight == Position.L1) {
            desiredHeight = 0.2;
        }
        else if (targetHeight == Position.L2) {
            desiredHeight = 0.17;
        }
        else if (targetHeight == Position.L3) {
            desiredHeight = 0.55;
        }
        else if (targetHeight == Position.L4) {
            desiredHeight = 1.1;
        }

        double pidOutput = pid.calculate(getHeight(), desiredHeight);
        
        // Clamp output to the elevator's MAX and MIN
        double motorOutput = Math.min(Math.max(pidOutput, Constants.Elevator.maxSpeedDown), Constants.Elevator.maxSpeedUp);
        
        // Add gravity compensation
        motorOutput = pidOutput + GRAVITY_COMPENSATION;
        
        // Clamp the output (again) to valid range
        motorOutput = Math.min(Math.max(motorOutput, -1.0), 1.0);

        // If the elevator is within the set range of the target height, set the last known position to that height. (it's approximately there)
        if (motorOutput < 0.1 && motorOutput > -0.1) {
            lastKnownPosition = targetHeight;
        } 
        
        manualShift(motorOutput);
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
        manualShift(GRAVITY_COMPENSATION);
    }

    /*
     * Feed the motors, avoiding motorsafety triggers.
     */
    public void feed() {
        m_elevatorLeft.feed();
        m_elevatorRight.feed();
    }
}
