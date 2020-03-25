package br.mssjim.hidroponia;

public class Status {
    private String status;
    private long lastSeen;

    public Status() {
    }

    public Status(String status, long lastSeen) {
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public long getLastSeen() {
        return lastSeen;
    }
}
