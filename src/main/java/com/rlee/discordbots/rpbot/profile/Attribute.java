package com.rlee.discordbots.rpbot.profile;

public abstract class Attribute<T> {
    private CharProfile profile;

    private String name;

    public Attribute(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the profile
     */
    public CharProfile getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(CharProfile profile) {
        this.profile = profile;
    }

    /**
     * @return the value of this attribute
     */
    public abstract T getValue();

    public abstract void setValue(T value);
}
