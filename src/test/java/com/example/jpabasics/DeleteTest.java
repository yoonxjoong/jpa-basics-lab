package com.example.jpabasics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpabasics.domain.Member;
import com.example.jpabasics.domain.MemberStatus;
import com.example.jpabasics.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DeleteTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Test
    void 물리_삭제하면_DB에서_행_자체가_사라진다() {
        Member saved = memberRepository.save(
            Member.builder().email("del@test.com").name("삭제될회원").build());
        em.flush();

        memberRepository.deleteById(saved.getId());
        em.flush();

        assertThat(memberRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void 논리_삭제는_행은_남기고_상태만_바꾼다_소프트_삭제() {
        Member saved = memberRepository.save(
            Member.builder().email("withdraw@test.com").name("탈퇴할회원").build());
        em.flush();
        em.clear();

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        found.withdraw(); // DELETE가 아니라 status만 WITHDRAWN으로 바꾸는 더티체킹
        em.flush();
        em.clear();

        // 물리 삭제와 달리 행 자체는 그대로 남아있음 -> 이력 조회, 다른 테이블의 참조 무결성 보존
        Member reloaded = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }
}
