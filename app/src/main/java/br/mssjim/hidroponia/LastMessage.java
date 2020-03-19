package br.mssjim.hidroponia;

public class LastMessage {
    private User user;
    private Message message;

    public LastMessage() {
    }

    public LastMessage(User user, Message message) {
        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
