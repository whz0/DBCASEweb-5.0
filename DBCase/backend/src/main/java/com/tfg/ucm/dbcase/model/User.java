package com.tfg.ucm.dbcase.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Debe usarse Long ya que si no existe devuelve null,

    private String username;
    private String password;

    @Column(columnDefinition = "TEXT")
    private String chart;

    @Column(name = "picture_url", columnDefinition = "TEXT")
    private String pictureUrl;

    @Embedded private UserSettings settings;

    /** Null means the account never expires (admin / seeded users). */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ElementCollection
    @CollectionTable(name = "user_custom_domains")
    private List<UserDomain> customDomains = new ArrayList<>();
}
