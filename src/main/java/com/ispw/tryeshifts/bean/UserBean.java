package com.ispw.tryeshifts.bean;

import com.ispw.tryeshifts.entity.Membership;

public class UserBean {
    private String email;
    private String password;
    private String name;
    private String surname;
    private Membership membership;
    private String Role;
    private String pwdRepeat;

    public UserBean(String text, String passwordFieldText){
        this.email = text;
        this.password = passwordFieldText;
    }
    public UserBean(String email, String password, String name, String surname){
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public UserBean(String email, String password, String name, String surname,String pwd2){
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.pwdRepeat = pwd2;
    }

    public UserBean(String email, String password, String name, String surname,Membership membership){
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.membership = membership;
    }

    public UserBean() {

    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public void setRole(String role){this.Role = role;}
    public String getRole(){return this.Role;}
    public String getPwdRep(){return pwdRepeat;}

}
