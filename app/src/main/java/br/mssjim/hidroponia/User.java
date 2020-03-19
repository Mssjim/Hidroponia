package br.mssjim.hidroponia;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userId;
    private String username;
    private String password;
    private String profileImage;

    public User() {
    }

    public User(String userId, String username, String password, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.profileImage = profileImage;
    }

    protected User(Parcel in) {
        userId = in.readString();
        username = in.readString();
        password = in.readString();
        profileImage = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(profileImage);
    }
}
