package br.mssjim.hidroponia;

public class LastMessage {
    private Message message;
    private String username;
    private String profileImage;

    public LastMessage() {
    }

    public LastMessage(Message message, String username, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
