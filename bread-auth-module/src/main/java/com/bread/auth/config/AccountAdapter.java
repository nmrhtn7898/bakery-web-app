package com.bread.auth.config;

import com.bread.auth.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import static java.util.stream.Collectors.toList;

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
                        .collect(toList())
        );
        this.account = account;
    }

}
