package de.freifunkdresden.viewerbackend;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airtime airtime = (Airtime) o;
        return active == airtime.active &&
                busy == airtime.busy &&
                receive == airtime.receive &&
                transmit == airtime.transmit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, busy, receive, transmit);
    }
}
