package com.netcracker.superproject.entity;

import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;

import java.util.Date;

@Entity(type = "1")
public class User extends BaseEntity{

    @Attribute(type = "002")
    private String email;

    @Attribute(type = "003")
    private String password;

    @Attribute(type = "004")
    private String role;

    @Attribute(type = "005")
    private String firstName;

    @Attribute(type = "006")
    private String lastName;

    @Attribute(type = "007")
    private String photo;

    @Attribute(type = "008")
    private String location;

    @Attribute(type = "009")
    private Date birthdayDate;

    @Attribute(type = "010")
    private Date registrationDate;

    @Attribute(type = "011")
    private String socialNetworks;

    public User() {
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String plastName) {
        this.lastName = plastName;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getBirthdayDate() {
        return this.birthdayDate;
    }

    public void setBirthdayDate(Date birthdayDate) {
        this.birthdayDate = birthdayDate;
    }

    public Date getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getSocialNetworks() {
        return this.socialNetworks;
    }

    public void setSocialNetworks(String socialNetworks) {
        this.socialNetworks = socialNetworks;
    }
}
