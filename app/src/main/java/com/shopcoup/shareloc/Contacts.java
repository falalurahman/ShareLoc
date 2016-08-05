package com.shopcoup.shareloc;

public class Contacts {
    String Name;
    String PhoneNumber;
    boolean HasAddress;

    public boolean isHasAddress() {
        return HasAddress;
    }

    public void setHasAddress(boolean hasAddress) {
        HasAddress = hasAddress;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
