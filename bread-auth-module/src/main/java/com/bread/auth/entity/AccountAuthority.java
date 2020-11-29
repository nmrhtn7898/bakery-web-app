package com.bread.auth.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * 게정, 권한 엔티티 매핑 엔티티
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = PROTECTED)
public class AccountAuthority extends BaseEntity {

    private static final long serialVersionUID = 1750462964682286485L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    private Account account;

    @ManyToOne(fetch = LAZY, optional = false, cascade = PERSIST)
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
