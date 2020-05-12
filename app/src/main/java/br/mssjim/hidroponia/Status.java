package br.mssjim.hidroponia;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Status {
    private boolean online;
    private long lastSeen;

    public Status() {
    }

    public Status(boolean online, long lastSeen) {
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        if(online) {
            return "Online"; // TODO Internacionalizar Texto
        } else {
            Date date = new Date(lastSeen);
            String s = "Visto em " +
                    new SimpleDateFormat("d/M").format(date) +
                    " às " +
                    new SimpleDateFormat("k:mm").format(date); // TODO Internacionalizar Texto

            return s;
        }
    }

    public long getLastSeen() {
        return lastSeen;
    }
}
