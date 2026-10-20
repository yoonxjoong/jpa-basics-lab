# JPA Basics Lab

> Spring Data JPA 기본 CRUD를 **실무에서 자주 만나는 함정** 위주로 테스트하며 익히는 실습 프로젝트
>
> 더티 체킹, 벌크 업데이트와 영속성 컨텍스트 불일치, 소프트 삭제, 유니크 제약 위반 시점 등을 테스트 코드로 확인합니다.

<br>

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3, Spring Data JPA (Hibernate 6) |
| DB | PostgreSQL 16 (Docker Compose) |
| Test | JUnit 5, AssertJ |

<br>

## 도메인

`Member`: 이메일(유니크), 이름, 상태(`ACTIVE` / `WITHDRAWN`), 가입일

- 기본 생성자는 JPA용으로만 열어 두고, 생성은 `@Builder` 생성자로만 합니다.
- 상태 변경은 setter 대신 `changeName()`, `withdraw()` 같은 **의도가 드러나는 메서드**로 합니다.

<br>

## 확인한 내용

### Create: `CreateTest`

| 테스트 | 배운 점 |
|---|---|
| 단건 저장 | `IDENTITY` 전략은 `save()` 시점에 바로 INSERT가 나가고 ID가 채워짐 |
| 여러 건 저장 | `saveAll()` |
| 이메일 중복 | `saveAndFlush()`로 즉시 반영해야 제약 조건 위반(`DataIntegrityViolationException`) 시점이 명확함 |

### Read: `ReadTest`

| 테스트 | 배운 점 |
|---|---|
| 없는 ID 조회 | `findById()`는 `Optional`을 반환해서, 없을 수 있다는 사실을 타입으로 강제함 |
| 파생 쿼리 | `findByEmail`, `findByNameContaining`처럼 메서드 이름만으로 쿼리 생성 |
| 존재 여부 | `existsByEmail()`은 엔티티 전체를 가져오지 않아 `findBy...().isPresent()`보다 가벼움 |

### Update: `UpdateTest`

| 테스트 | 배운 점 |
|---|---|
| 더티 체킹 | 영속 상태 엔티티의 필드만 바꾸면 `save()`를 다시 부르지 않아도 flush 시점에 UPDATE |
| **벌크 업데이트 함정** | `@Modifying` JPQL은 영속성 컨텍스트를 거치지 않음. DB는 바뀌었는데 **이미 로딩된 객체는 옛 값 그대로**라서 `em.clear()` 후 다시 조회해야 최신값이 보임 |

### Delete: `DeleteTest`

| 테스트 | 배운 점 |
|---|---|
| 물리 삭제 | `deleteById()`로 행 자체 삭제 |
| **논리 삭제(소프트 삭제)** | 상태만 `WITHDRAWN`으로 바꿔 이력과 다른 테이블의 참조 무결성을 보존. 실무에서 더 자주 씀 |

<br>

## 실행 방법

```bash
docker compose up -d   # PostgreSQL (localhost:5433)
./gradlew test         # 실행된 SQL과 바인딩 값이 로그로 출력됨
```

각 테스트는 `@Transactional`로 감싸 끝나면 롤백됩니다.

<br>

## 다음 단계

- 연관 관계와 N+1 문제 → [jpa-n-plus-one-lab](https://github.com/yoonxjoong/jpa-n-plus-one-lab)
- 트랜잭션 전파 → [transaction-propagation-lab](https://github.com/yoonxjoong/transaction-propagation-lab)
