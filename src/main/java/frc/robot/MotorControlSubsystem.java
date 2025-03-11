package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

public class MotorControlSubsystem {
    WPI_VictorSPX m_shooter = new WPI_VictorSPX(3);
    WPI_VictorSPX m_loader = new WPI_VictorSPX(1);
    WPI_VictorSPX m_climber = new WPI_VictorSPX(2);
    WPI_VictorSPX m_intake = new WPI_VictorSPX(8);

    private double m_intakeSet;
    private double m_shooterSet;
    private double m_loaderSet;
    private double m_climberSet;

    public MotorControlSubsystem() {}

    public void SpinUpShooter() {
        m_shooterSet = 1.0;
        m_shooter.set(m_shooterSet);
    }

    public void ShootNote() {
        m_shooterSet = 1.0;
        m_loaderSet = 1.0;
        m_intakeSet = -0.7;

        m_shooter.set(m_shooterSet);
        m_loader.set(m_loaderSet);
        m_intake.set(m_intakeSet);
    }

    public void IntakeNote() {
        m_intakeSet = -0.7;

        m_intake.set(m_intakeSet);
    }

    public void EjectNote() {
        m_intakeSet = 0.7;
        m_intake.set(m_intakeSet);
    }
}
