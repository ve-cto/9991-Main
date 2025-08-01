## Operator Notes
This year's Robot code is organised into seperate files, instead of just being a glob in one large file.
- Robot.java contains the majority of the Robot code. It is responsible for dispatching commands, by detecting controller inputs and sendingmout commands to subsystems. This is also where Autonomous tasks are located.
- Constants.java contains a set of variables that remain constant, such as device assignments, and compensation values. Having them in this seperste file allows for easy and simple tweaking, without having to code-dive.

- Maps
    - ControllerMap.java contains a large list of getters for controller values. 

- Commands (General)
    - Contains methods for controlling and measuring different parts of the Robot.
        - Algae.java
        - Elevator.java
        - EndEffector.java
        - DriveSubsystem.java **rename**
        - LimelightDriveSubsystem.java **rename**