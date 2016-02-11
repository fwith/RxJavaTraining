package com.kakao.izzy.rxjavatraining;

import android.graphics.drawable.Drawable;

/**
 * Created by izzy on 16. 2. 11..
 */
public class User {
    private final Drawable profile;
    private final String firstName;
    private final String lastName;

    public User(Drawable profile, String firstName, String lastName) {
        this.profile = profile;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Drawable getProfile() {
        return profile;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
