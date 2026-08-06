package com.example.jpabasics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpabasics.domain.Member;
import com.example.jpabasics.domain.MemberStatus;
import com.example.jpabasics.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UpdateTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Test
    void save를_다시_호출하지_않아도_필드를_바꾸면_반영된다_더티체킹() {
        Member saved = memberRepository.save(
            Member.builder().email("dirty@test.com").name("원래이름").build());
        em.flush();
        em.clear(); // 1차 캐시를 비워서, 아래 findById가 진짜 새 조회를 하게 만듦

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        found.changeName("바뀐이름"); // memberRepository.save(found)를 호출하지 않았음!

        em.flush(); // 이 시점에 Hibernate가 "저장할 때와 지금 상태가 다르네"를 감지해서 UPDATE를 날림
        em.clear();

        Member reloaded = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("바뀐이름"); // DB에 실제로 반영됨
    }

    @Test
    void 벌크_업데이트는_1차_캐시를_안_거쳐서_이미_로딩된_객체가_안_바뀐다() {
        Member old = memberRepository.save(
            Member.builder().email("old@test.com").name("오래된회원").build());
        em.flush();

        assertThat(old.getStatus()).isEqualTo(MemberStatus.ACTIVE); // 아직 1차 캐시에 로딩된 상태

        int updatedCount = memberRepository.bulkUpdateStatus(
            MemberStatus.ACTIVE, MemberStatus.WITHDRAWN, LocalDateTime.now().plusSeconds(1));

        assertThat(updatedCount).isEqualTo(1); // DB에는 1건 UPDATE됨

        // 함정: 벌크 쿼리는 영속성 컨텍스트를 거치지 않고 DB에 직접 SQL을 날리기 때문에,
        // 메모리에 이미 들고 있던 old 객체는 갱신되지 않고 여전히 ACTIVE로 보임
        assertThat(old.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        em.clear(); // 캐시를 비우고 다시 조회해야 진짜 최신값을 봄
        Member reloaded = memberRepository.findById(old.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }
}
