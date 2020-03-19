package br.mssjim.hidroponia;

import android.os.Parcel;
import android.os.Parcelable;

public class Group implements Parcelable {
    private String username;
    private String profileImage;

    public Group() {
    }

    public Group(String username, String profileImage) {
        this.username = username;
        this.profileImage = profileImage;
    }

    protected Group(Parcel in) {
        username = in.readString();
        profileImage = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getUsername() {
        return username;
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
        parcel.writeString(username);
        parcel.writeString(profileImage);
    }
}
