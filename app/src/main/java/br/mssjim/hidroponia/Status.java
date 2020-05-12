package br.mssjim.hidroponia;

public class Status {
    // TODO boolean 'Online'
    private String status;
    private long lastSeen;

    public Status() {
    }

    public Status(String status, long lastSeen) {
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        // TODO Retornar Last Seen caso 'status == Offline'
        return status;
    }

    public long getLastSeen() {
        return lastSeen;
    }
}
