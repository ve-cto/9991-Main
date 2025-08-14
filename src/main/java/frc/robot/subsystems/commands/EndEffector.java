package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import frc.robot.Constants;
import edu.wpi.first.math.controller.ControlAffinePlantInversionFeedforward;
import edu.wpi.first.wpilibj.DigitalInput;

public class EndEffector {
    private WPI_VictorSPX m_intake1;
    private WPI_VictorSPX m_intake2;
    private DigitalInput s_break;

    private boolean isCoralLoaded;
    private boolean isCoralPastStage1;

    
    

    public EndEffector() {
        m_intake1 = new WPI_VictorSPX(Constants.EndEffector.m_intake1ID);
        m_intake2 = new WPI_VictorSPX(Constants.EndEffector.m_intake2ID);

        s_break = new DigitalInput(Constants.EndEffector.s_breakID);
        

        isCoralLoaded = false;
        isCoralPastStage1 = false;
    }
    
    public boolean getEndstop() {
        return s_break.get();
    }
    
    public boolean getCoralLoaded() {
        return isCoralLoaded;
    }

    public String getCoralState() {
        if (isCoralPastStage1 == true && isCoralLoaded == false) {
            return "Partly Staged";
        } else if (isCoralLoaded == true) {
            return "Staged";
        } else {
            return "Unstaged";
        }
    }

    public void intakeCoral() {
        // Only intake if the coral is not loaded (persists)
        if (!isCoralLoaded) {
            if (isCoralPastStage1 == true && isCoralLoaded == false) {
                if (!s_break.get()) {
                    manualShift(0.32);
                } else {
                    isCoralLoaded = true;
                    manualShift(0.0);
                }

            } else if (isCoralLoaded == false && isCoralPastStage1 == false) {
                // If the endstop isn't broken....
                if (s_break.get()) {
                    manualShift(0.5);
                    isCoralPastStage1 = false;
                } else {
                    isCoralPastStage1 = true;
                }
            }
            
        } else {
            manualShift(0.0);
        }
    }

    public void releaseCoral() {
        // If no coral is loaded to release, intake coral.
            manualShift(0.4);
            isCoralLoaded = false;
            isCoralPastStage1 = false;
    }

    public void manualShift(double speed) {
        m_intake1.set(-speed);
        m_intake2.set(speed);
    }

    public void stop() {
        m_intake1.set(0.0);
        m_intake2.set(0.0);
    }

    /*
     * Set the coral state to one of three states. 
     * Only to be used for debugging.
     * state 0 = unloaded
     * state 1 = partly staged
     * state 2 = staged
     */
    public void debugState(int state) {
        if (state == 0) {
            isCoralLoaded = false;
            isCoralPastStage1 = false;
        } else if (state == 1) {
            isCoralLoaded = false;
            isCoralPastStage1 = true;
        } else if (state == 3) {
            isCoralLoaded = true;
            isCoralPastStage1 = false;
        }
    }
}