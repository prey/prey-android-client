package com.prey.ble.db;


import android.support.annotation.NonNull;


public class KeyDto implements Comparable {


    private String name;
    private String alias;
    private String address;
    private int image;

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(" name:").append(name);
        sb.append(" alias:").append(alias);
        sb.append(" address:").append(address);
        sb.append(" image:").append(image);
        return sb.toString();
    }

    @Override
    public int compareTo(@NonNull Object another) {
        KeyDto other = (KeyDto) another;
        return alias.compareTo(other.alias);
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}