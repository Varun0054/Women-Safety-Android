package com.example.womensafe;

public class people {
    String person_name;
    String person_mobile;
    String member_type;
            String uri;


    public people() {
    }

    public people(String person_name, String person_mobile, String uri, String member_type) {
        this.person_name = person_name;
        this.person_mobile = person_mobile;
        this.uri = uri;
        this.member_type=member_type;
    }

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public String getPerson_mobile() {
        return person_mobile;
    }

    public void setPerson_mobile(String person_mobile) {
        this.person_mobile = person_mobile;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMember_type() {
        return member_type;
    }

    public void setMember_type(String member_type) {
        this.member_type = member_type;
    }
}
