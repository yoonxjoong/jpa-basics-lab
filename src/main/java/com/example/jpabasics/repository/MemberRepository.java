package com.example.jpabasics.repository;

import com.example.jpabasics.domain.Member;
import com.example.jpabasics.domain.MemberStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메서드 이름만으로 쿼리가 자동 생성됨 (파생 쿼리) -> WHERE email = ?
    Optional<Member> findByEmail(String email);

    // WHERE status = ?
    List<Member> findByStatus(MemberStatus status);

    // WHERE name LIKE %keyword%
    List<Member> findByNameContaining(String keyword);

    // SELECT COUNT(*) > 0 형태로 최적화된 존재 여부 확인 (엔티티 전체를 안 끌고 옴)
    boolean existsByEmail(String email);

    // 벌크 업데이트: 영속성 컨텍스트(1차 캐시)를 거치지 않고 DB에 직접 UPDATE를 날림.
    // 더티 체킹과 달리 "몇 건이 바뀌었는지"만 리턴되고, 이미 메모리에 로딩된 엔티티들은
    // 자동으로 갱신되지 않음(이 함정은 UpdateTest에서 직접 확인).
    @Modifying
    @Query("update Member m set m.status = :to where m.status = :from and m.createdAt < :before")
    int bulkUpdateStatus(MemberStatus from, MemberStatus to, LocalDateTime before);
}
