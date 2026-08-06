package com.example.jpabasics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Member(String email, String name) {
        this.email = email;
        this.name = name;
        this.status = MemberStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public void changeName(String name) {
        this.name = name;
    }

    // 실무에서는 DELETE보다 이런 "논리 삭제(soft delete)"를 더 많이 씀 —
    // 다른 테이블이 이 회원을 참조 중일 수도 있고, 탈퇴해도 이력은 남겨야 하는 경우가 많아서.
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }
}
