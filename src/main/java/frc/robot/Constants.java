package frc.robot;

public class Constants {
    public static class Robot {
        public static double driveSpeedSlow = 0.6;
        public static double driveSpeedNormal = 0.7;
        public static double driveSpeedFast = 0.8;
        public static double driveSpeedMax = 1.0;
    }

    public static class Elevator {
        public static final int sEncoderID1 = 0;
        public static final int sEncoderID2 = 1;

        public static final int sEndstopID = 2;

        public static final int m_elevatorLeftID = 4;
        public static final int m_elevatorRightID = 5;

        public static final double gravityComp = 0.05;
        public static final double countsPer1M = 7850.0;
    }

    public static class Algae {
        public static final int m_armID = 8;
        public static final int m_grabberID = 7;

    }

    public static class EndEffector {
        public static final int s_break1ID = 3;
        public static final int s_break2ID = 4;

        public static final int m_intake1ID = 0;
        public static final int m_intake2ID = 6;
    }

    public static class Drive {
        public static final int m_driveFLID = 0;
        public static final int m_driveFRID = 0;
        public static final int m_driveBLID = 0;
        public static final int m_driveBRID = 0;
    }

    public static class Field {


    }
}
