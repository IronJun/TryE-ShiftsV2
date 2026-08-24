package com.ispw.tryeshifts.entity;

public class UserInfo{
    private String name;
    private String surname;
    private String email;
    private String passwordHash;


    public UserInfo(){}
    public UserInfo(String email, String name,String surname){
        this.name = name;
        this.surname = surname;
        this.email = email;

    }


    public void setName(String name){
        this.name = name;
    }
    public void setSurname(String surname){
        this.surname = surname;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getName(){
        return this.name;
    }
    public String getSurname(){
        return this.surname;
    }
    public String getEmail(){
        return this.email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    // Setter per il passwordHash
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

}
