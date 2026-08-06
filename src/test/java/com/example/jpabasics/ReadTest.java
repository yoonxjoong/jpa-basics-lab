package com.example.jpabasics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpabasics.domain.Member;
import com.example.jpabasics.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReadTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 존재하지_않는_ID로_조회하면_Optional이_비어있다() {
        Optional<Member> found = memberRepository.findById(999_999L);

        assertThat(found).isEmpty();
        // 실무 패턴: found.orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다"))
        // null 체크 대신 Optional로 "없을 수도 있음"을 타입으로 강제하는 게 findById()의 핵심
    }

    @Test
    void 이메일로_조회한다_파생쿼리() {
        memberRepository.save(Member.builder().email("find@test.com").name("찾기대상").build());

        Optional<Member> found = memberRepository.findByEmail("find@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("찾기대상");
    }

    @Test
    void 이름에_특정_문자열이_포함된_회원을_검색한다() {
        memberRepository.save(Member.builder().email("kim1@test.com").name("김철수").build());
        memberRepository.save(Member.builder().email("kim2@test.com").name("김영희").build());
        memberRepository.save(Member.builder().email("lee1@test.com").name("이몽룡").build());

        List<Member> result = memberRepository.findByNameContaining("김");

        assertThat(result).hasSize(2);
    }

    @Test
    void 이메일_존재여부만_확인한다_엔티티_전체를_안_끌고옴() {
        memberRepository.save(Member.builder().email("exist@test.com").name("존재회원").build());

        assertThat(memberRepository.existsByEmail("exist@test.com")).isTrue();
        assertThat(memberRepository.existsByEmail("nobody@test.com")).isFalse();
        // existsBy...는 내부적으로 SELECT count(*) > 0 형태로 나가서, findBy...().isPresent()보다
        // 컬럼을 덜 읽어옴 - "있는지 없는지"만 필요할 땐 이쪽이 더 적절함
    }
}
