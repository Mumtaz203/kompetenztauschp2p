package de.thws.kompetenz.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_offered_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", nullable = false, length = 100)
    private List<String> offeredSkills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_wanted_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", nullable = false, length = 100)
    private List<String> wantedSkills = new ArrayList<>();

    public UserEntity() {
    }

    public UserEntity(UUID id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<String> getOfferedSkills() { return offeredSkills; }
    public void setOfferedSkills(List<String> offeredSkills) { this.offeredSkills = offeredSkills; }

    public List<String> getWantedSkills() { return wantedSkills; }
    public void setWantedSkills(List<String> wantedSkills) { this.wantedSkills = wantedSkills; }
}
