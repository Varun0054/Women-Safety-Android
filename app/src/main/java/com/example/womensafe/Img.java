package com.example.womensafe;

public class Img {

    String name ;
    String mobile ;
    String uri;
    String address ;
    String dob;
    String adhar;
    String gender;
    String uid;

    public Img() {
    }

    public Img(String name, String mobile,String uri, String address, String dob, String adhar, String gender,String uid) {
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.uri = uri;
        this.dob = dob;
        this.adhar = adhar;
        this.gender = gender;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }




    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAdhar() {
        return adhar;
    }

    public void setAdhar(String adhar) {
        this.adhar = adhar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
