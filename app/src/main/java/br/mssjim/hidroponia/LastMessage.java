package br.mssjim.hidroponia;

public class LastMessage {
    // TODO Remover username e image (Trabalhar apenas com o ID)
    private Message message;
    private String userId;
    private String username;
    private String profileImage;

    public LastMessage() {
    }

    public LastMessage(Message message, String userId, String username, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
        this.message = message;
        this.userId = userId;
    }

    public Message getMessage() {
        return message;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
