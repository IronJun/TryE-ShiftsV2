package com.ispw.tryeshifts.entity;

public class UserInfo{
    private String nome;
    private String cognome;
    private String email;
    private String passwordHash;


    public UserInfo(){}
    public UserInfo(String email, String name,String surname){
        this.nome = name;
        this.cognome = surname;
        this.email = email;

    }

    public void setName(String name){
        this.nome = name;
    }
    public void setSurname(String surname){
        this.cognome = surname;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getName(){
        return this.nome;
    }
    public String getSurname(){
        return this.cognome;
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
