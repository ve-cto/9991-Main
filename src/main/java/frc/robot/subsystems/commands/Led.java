package frc.robot.subsystems.commands;

import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.wpilibj.DigitalOutput;

import frc.robot.Constants;

public class Led {
    private Timer flashTimer;
    private boolean isFlashing;

    DigitalOutput arduino1 = new DigitalOutput(Constants.Led.o_arduino1ID);
    DigitalOutput arduino2 = new DigitalOutput(Constants.Led.o_arduino2ID);
    DigitalOutput arduino3 = new DigitalOutput(Constants.Led.o_arduino3ID);

    private Constants.Led.StatusList Status;

    public Led() {
        this.isFlashing = false;
    }

    public Constants.Led.StatusList getStatus() {
        return this.Status;
    }

    public void setAndApplyStatus(Constants.Led.StatusList desiredStatus) {
        setStatus(desiredStatus);
    }

    public void setStatus(Constants.Led.StatusList desiredStatus) {
        this.Status = desiredStatus;
        switch (desiredStatus) {
            case DISCONNECT:
                sendData(1);
            case DISABLED:
                sendData(2);
            case IDLE:
                sendData(3);
            case AUTONOMOUS:
                sendData(4);
            case LOADED:
                sendData(5);
            case READY:
                sendData(6);
            case BLANK:
                sendData(0);
        }
        // System.out.println("LED's Status has been changed");
    }

    public void flashStatus(Constants.Led.StatusList desiredStatus, int numFlashes, double flashSpeed) {
        if (this.isFlashing = false) {
            this.isFlashing = true;
            flashTimer.restart();
            System.out.println("Started to Flash");
            for (int i = 0; i < numFlashes+1; i++) {
                if (flashTimer.get() < flashSpeed) {
                    setStatus(desiredStatus);
                } else if (flashTimer.get() < flashSpeed * 2) {
                    setStatus(Constants.Led.StatusList.BLANK);
                } else {
                    flashTimer.restart();
                }
            }
            System.out.println("Flashing Completed");
            this.isFlashing = false;
        }
    }

    public boolean getFlashing() {
        return (this.isFlashing);
    }

    // split an integer i into individual bits and send to the Arduino    
    // maximum i = 2^n - 1
    // (https://sites.google.com/albany.k12.ny.us/team1493programming/add-an-arduino)
    public void sendData(int i){
        // boolean b1=false,b2=false,b3=false;
        // I have no idea how this works.
        
        arduino1.set(((i>>2)&1) ==1);
        arduino2.set(((i>>1)&1) ==1);
        arduino3.set((i&1) == 1);
    }

    public void reset() {}
}