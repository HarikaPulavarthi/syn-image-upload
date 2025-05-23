package com.synchrony.cloudinary.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
@Schema(description = "User entity representing a user in the system.")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "unique identifier for user")
    private Long id;

    @Column(nullable = false,unique = true)
    @Schema(description = "username of the user")
    private String username;

    @Column(nullable = false)
    @Schema(description = "password of the user")
    private String password;

    @Schema(description = "name of the user")
    private String name;
    @Schema(description = "email of the user")
    private String email;



    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "list of images uploaded by the user")
    private List<Image> images;

    public User(String username, String password, String name, String email){
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }

}
