package com.example.jpabasics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jpabasics.domain.Member;
import com.example.jpabasics.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CreateTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 단건_저장하면_INSERT가_나가고_ID가_자동으로_채워진다() {
        Member member = Member.builder().email("a@test.com").name("홍길동").build();
        assertThat(member.getId()).isNull(); // 아직 저장 전이라 ID가 없음

        Member saved = memberRepository.save(member);

        // GenerationType.IDENTITY라 save() 호출 시점에 곧바로 INSERT가 나가고 ID가 채워짐
        // (SEQUENCE 전략이었으면 이 시점이 좀 달라짐 - IDENTITY만 즉시 flush됨)
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 여러_건을_한번에_저장한다() {
        List<Member> members = List.of(
            Member.builder().email("b1@test.com").name("회원1").build(),
            Member.builder().email("b2@test.com").name("회원2").build()
        );

        List<Member> saved = memberRepository.saveAll(members);

        assertThat(saved).hasSize(2);
        assertThat(memberRepository.count()).isEqualTo(2);
    }

    @Test
    void 이메일이_중복되면_저장_시점에_제약조건_위반_예외가_난다() {
        memberRepository.save(Member.builder().email("dup@test.com").name("회원A").build());
        Member duplicated = Member.builder().email("dup@test.com").name("회원B").build();

        // saveAndFlush로 즉시 DB에 반영시켜서, unique 제약이 걸리는 시점을 명확히 함
        assertThatThrownBy(() -> memberRepository.saveAndFlush(duplicated))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
