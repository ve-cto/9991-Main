package frc.robot.subsystems.commands;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import frc.robot.Constants;

public class Algae {
    WPI_VictorSPX m_grabber;
    WPI_VictorSPX m_arm;


    public Algae(){
        m_grabber = new WPI_VictorSPX(Constants.Algae.m_grabberID);

        m_arm = new WPI_VictorSPX(Constants.Algae.m_armID);
    }

    public void manualShiftArm(double speed) {
        m_arm.set(speed);
    }

    public void manualShiftGrabber(double speed) {
        m_grabber.set(speed);
    }

    public void feed() {
        m_arm.feed();
        m_grabber.feed();
    }

    public void stopArm() {
        m_arm.set(0.0);
    }

    public void stopGrabber() {
        m_grabber.set(0.0);
    }
}
