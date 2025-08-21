package frc.robot;

public class Constants {
    public static class Robot {}

    public static class Elevator {
        public static final int sEncoderID1 = 0;
        public static final int sEncoderID2 = 1;

        public static final int sEndstopID = 2;

        public static final int m_elevatorLeftID = 1;
        public static final int m_elevatorRightID = 2;

        public static final double gravityComp = -0.05;
        public static final double countsPer1M = 7850.0;

        public static final double maxSpeedDown = -0.4;
        public static final double maxSpeedUp = 0.7;

        public static final double kp = 3.0;
        public static final double ki = 0.0;
        public static final double kd = 0.7;

        public enum Position {
            HOME,
            L1,
            L2,
            L3,
            L4,
            UNKNOWN
        }
    }

    public static class Algae {
        public static final int m_armID = 5;
        public static final int m_grabberID = 10;

    }

    public static class EndEffector {
        public static final int s_breakID = 3;

        public static final int m_intake1ID = 3;
        public static final int m_intake2ID = 4;
    }

    public static class Drive {
        public static final int m_driveFLID = 8;
        public static final int m_driveFRID = 6;
        public static final int m_driveBLID = 9;
        public static final int m_driveBRID = 7;

        public static final double driveSpeedElevator = 0.4;
        public static final double driveSpeedL1 = 0.4;
        public static final double driveSpeedNormal = 0.6;
        public static final double driveSpeedFast = 0.7;
        public static final double driveSpeedFaster = 0.9;
        public static final double driveSpeedMax = 1.0;

        public static final double turnMultiplier = 1.2;
    }

    public static class Field {}

    public static class Led {
        public static final int l_ledID = 0;
        
        public static enum StatusList {
            DISCONNECT,
            DISABLED,
            IDLE,
            IDLERED,
            IDLEBLUE,
            AUTONOMOUS,
            LOADED,
            READY,
            RELEASE,
            UNSAFE,
            BLANK
        }
    }


}
