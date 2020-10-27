package com.bread.auth.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

/**
 * 게정, 권한 엔티티 매핑 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountAuthority extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    private Account account;

    @ManyToOne(fetch = LAZY, optional = false)
    private Authority authority;

    @Builder
    public AccountAuthority(Long id, Account account, Authority authority) {
        setAccount(account);
        this.id = id;
        this.authority = authority;
    }

    public void setAccount(Account account) {
        this.account = account;
        this.account.addAuthority(this);
    }

}
