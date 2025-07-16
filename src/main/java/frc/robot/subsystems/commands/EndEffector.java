package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Encoder;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.DigitalInput;

public class EndEffector {
    private WPI_VictorSPX m_intake;
    private DigitalInput s_break1;
    private DigitalInput s_break2;

    private boolean isCoralLoaded;
    private boolean isCoralPastStage1;

    public EndEffector() {
        m_intake = new WPI_VictorSPX(Constants.EndEffector.m_intakeID);

        s_break1 = new DigitalInput(Constants.EndEffector.s_break1ID);
        s_break2 = new DigitalInput(Constants.EndEffector.s_break2ID);
        isCoralLoaded = false;
        isCoralPastStage1 = false;
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
        isCoralPastStage1 = s_break1.get();
        isCoralLoaded = s_break2.get();

        if (isCoralLoaded == false && isCoralPastStage1 == false) {
            m_intake.set(0.5);
        } else if (isCoralLoaded == false && isCoralPastStage1 == true) {
            m_intake.set(0.2);
        } else {
            m_intake.set(0.0);
        }
    }

    public void releaseCoral() {
        if (isCoralLoaded == true) {
            m_intake.set(0.3);
        } else {
            intakeCoral();
        }
    }
}