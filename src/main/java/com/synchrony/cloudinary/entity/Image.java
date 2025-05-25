package com.synchrony.cloudinary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="images")
@Schema(description = "Image entity representing an image in the system.")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "unique identifier for image")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "public id of the image")
    private String publicId;
    @Schema(description = "url of the image")
    private String url;
    @Schema(description = "filename of the image")
    private String filename;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    @Schema(description = "user who uploaded the image")
    private User user;
}
