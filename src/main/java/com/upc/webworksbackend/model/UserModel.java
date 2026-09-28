package com.upc.webworksbackend.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@Table(name = "\"user\"")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "birthDate", nullable = false)
    private Date birthDate;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "photo")
    private String photo;

    @Column(name = "rol", nullable = false)
    private String rol = "DEVELOPER";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }


    ///relation-Subscription
    @OneToMany(mappedBy = "userSubscription",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<SubscriptionModel> subscriptions;

    ///relation-MethodPayment
    @OneToMany(mappedBy = "userMethodPayment",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<MethodPaymentModel> methodPayments;

    ///relation-Repository
    @OneToMany(mappedBy = "userRepository",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<RepositoryModel> repositories;

    ///relation-commentProfile
    @OneToMany(mappedBy = "userCommentProfile",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<CommentProfileModel> commentsProfile;

    ///relation-SystemScore
    @OneToMany(mappedBy = "userSystemScore",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<SystemScoreModel> systemScores;

    ///relation-JobApplication
    @OneToMany(mappedBy = "userJobApplication",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<JobApplicationModel> jobApplications;
}
