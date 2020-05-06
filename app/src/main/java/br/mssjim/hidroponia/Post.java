package br.mssjim.hidroponia;

public class Post {
    private String userId;
    private String text;
    private String image;
    private long time;

    public Post() {
    }

    public Post(String userId, String text, String image, long time) {
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public long getTime() {
        return time;
    }
}
