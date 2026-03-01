package ru.healthfamily.biobank.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.healthfamily.biobank.model.User;
import ru.healthfamily.biobank.model.UserPermissionOverride;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> effectivePermissions = getEffectivePermissionNames();
        return effectivePermissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public java.util.Set<String> getPermissionNames() {
        return getEffectivePermissionNames();
    }

    private static final Set<String> DEFAULT_VIEW_ONLY = Set.of(
            "patient.view", "visit.view", "sample.view", "research.view",
            "storage.view", "reference.view"
    );

    private Set<String> getEffectivePermissionNames() {
        Set<String> inherited = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()
                        .map(p -> p.getPermissionName()))
                .collect(Collectors.toSet());

        // Если у пользователя нет ролей — только просмотр (обычный пользователь)
        if (inherited.isEmpty()) {
            return new java.util.HashSet<>(DEFAULT_VIEW_ONLY);
        }

        Map<String, Boolean> overrides = user.getPermissionOverrides().stream()
                .collect(Collectors.toMap(
                        o -> o.getPermission().getPermissionName(),
                        UserPermissionOverride::getAllowed,
                        (a, b) -> b
                ));

        Set<String> effective = inherited.stream().collect(Collectors.toSet());
        for (var e : overrides.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                effective.add(e.getKey());
            } else {
                effective.remove(e.getKey());
            }
        }
        return effective;
    }
}
