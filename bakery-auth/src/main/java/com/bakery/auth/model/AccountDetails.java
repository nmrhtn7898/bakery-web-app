package com.bakery.auth.model;

import com.bakery.auth.entity.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.util.StringUtils.isEmpty;

@Getter
@ToString(exclude = "password", callSuper = true)
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = PROTECTED)
public class AccountDetails implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 2536690579355511927L;

    private Long id;

    private String username;

    private String password;

    private Set<GrantedAuthority> authorities;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private boolean enabled;

    public AccountDetails(Account account) {
        if (isEmpty(account.getEmail()) || account.getPassword() == null) {
            throw new IllegalArgumentException("cannot pass null or empty values to constructor");
        }

        this.id = account.getId();
        this.password = account.getPassword();
        this.username = account.getEmail();
        this.authorities = unmodifiableSet(
                account
                        .getAuthorities()
                        .stream()
                        .map(accountRole -> new SimpleGrantedAuthority(accountRole.getAuthority().getName()))
                        .collect(toSet())
        );
        this.accountNonExpired = true; // TODO
        this.accountNonLocked = true; // TODO
        this.credentialsNonExpired = true; // TODO
        this.enabled = true; // TODO
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

}
