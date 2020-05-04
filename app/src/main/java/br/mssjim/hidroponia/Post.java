package br.mssjim.hidroponia;

public class Post {
    private String text;
    private String image;
    private User user;
    private long time;

    public Post() {
    }

    public Post(User user, String text, String image, long time) {
        this.user = user;
        this.text = text;
        this.image = image;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }

    public User getUser() {
        return user;
    }

    public long getTime() {
        return time;
    }
}
