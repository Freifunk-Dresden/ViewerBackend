package de.freifunkdresden.viewerbackend;

public class Airtime {

    public static final Airtime EMPTY = new Airtime(0, 0, 0, 0);

    private final int active;
    private final int busy;
    private final int receive;
    private final int transmit;

    public Airtime(int active, int busy, int receive, int transmit) {
        this.active = active;
        this.busy = busy;
        this.receive = receive;
        this.transmit = transmit;
    }

    public int getActive() {
        return active;
    }

    public int getBusy() {
        return busy;
    }

    public int getReceive() {
        return receive;
    }

    public int getTransmit() {
        return transmit;
    }
}
