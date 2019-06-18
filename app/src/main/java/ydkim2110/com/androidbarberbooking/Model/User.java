package ydkim2110.com.androidbarberbooking.Model;

public class User {

    private String name;
    private String address;
    private String phoneNumber;

    public User() {
    }

    public User(String name, String address, String phoneNumber) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
