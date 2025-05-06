package zydd.org.libsmanagement.Member.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zydd.org.libsmanagement.Member.Model.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository <Member, Long>{
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
