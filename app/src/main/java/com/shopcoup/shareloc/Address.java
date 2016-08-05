package com.shopcoup.shareloc;


import com.google.android.gms.location.places.Place;

public class Address {

    String UID;
    String AddressName;
    String PhoneNumber;
    double VisualAddressLatitude;
    double VisualAddressLongitude;
    String TextualAddress;
    String AudioAddress;
    String AudioFileName;
    boolean isSetAudioAddress = false;
    boolean AudioAddressChanged = false;
    boolean isPublic;

    public String getAudioFileName() {
        return AudioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        AudioFileName = audioFileName;
    }

    public boolean isAudioAddressChanged() {
        return AudioAddressChanged;
    }

    public void setAudioAddressChanged(boolean audioAddressChanged) {
        AudioAddressChanged = audioAddressChanged;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public double getVisualAddressLatitude() {
        return VisualAddressLatitude;
    }

    public String getAddressName() {
        return AddressName;
    }

    public void setAddressName(String addressName) {
        AddressName = addressName;
    }

    public boolean isSetAudioAddress() {
        return isSetAudioAddress;
    }

    public void setIsSetAudioAddress(boolean isSetAudioAddress) {
        this.isSetAudioAddress = isSetAudioAddress;
    }

    public void setVisualAddressLatitude(double visualAddressLatitude) {
        VisualAddressLatitude = visualAddressLatitude;
    }

    public double getVisualAddressLongitude() {
        return VisualAddressLongitude;
    }

    public void setVisualAddressLongitude(double visualAddressLongitude) {
        VisualAddressLongitude = visualAddressLongitude;
    }

    public String getTextualAddress() {
        return TextualAddress;
    }

    public void setTextualAddress(String textualAddress) {
        TextualAddress = textualAddress;
    }

    public String getAudioAddress() {
        return AudioAddress;
    }

    public void setAudioAddress(String audioAddress) {
        AudioAddress = audioAddress;
    }

}
