package RealDevice.deviceTwins4Real;

public class fanStatus {
    private boolean isOn; // on or off
    private int speed; // (0, 100], reset -> 1
    private int angle; // [0, 120], reset -> 0

    public fanStatus() {
    }

    public fanStatus(boolean isOn, int speed, int angle) {
        this.isOn = isOn;
        this.speed = speed;
        this.angle = angle;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "fanStatus{" +
                "'isOn':" + isOn +
                ", 'speed':" + speed +
                ", 'angle':" + angle +
                "'}";
    }
}
