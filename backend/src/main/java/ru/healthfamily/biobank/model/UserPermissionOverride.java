package ru.healthfamily.biobank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_permission_overrides",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_permission_override",
                columnNames = {"user_id", "permission_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_permission_override_id")
    private Long userPermissionOverrideId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", referencedColumnName = "permissions_id", nullable = false)
    private Permission permission;

    @Column(name = "is_allowed", nullable = false)
    private Boolean allowed;
}

