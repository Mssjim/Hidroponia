package br.mssjim.hidroponia;

public class Message {
    private String text;
    private String userId;
    private String userSendId;
    private long time;

    public Message() {
    }

    public Message(String text, String userId, String userSendId, long time) {
        this.text = text;
        this.userId = userId;
        this.userSendId = userSendId;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserSendId() {
        return userSendId;
    }

    public long getTime() {
        return time;
    }
}
