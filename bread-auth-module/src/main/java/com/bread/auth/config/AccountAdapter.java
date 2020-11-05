package com.bread.auth.config;

import com.bread.auth.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Collectors;

@Getter
public class AccountAdapter extends User {

    private final Account account;

    public AccountAdapter(Account account) {
        super(
                account.getEmail(),
                account.getPassword(),
                account
                        .getAuthorities()
                        .stream()
                        .map(accountRole -> new SimpleGrantedAuthority(accountRole.getAuthority().getName()))
                        .collect(Collectors.toList())
        );
        this.account = account;
    }

}
