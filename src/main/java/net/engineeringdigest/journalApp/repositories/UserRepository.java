package net.engineeringdigest.journalApp.repositories;

import net.engineeringdigest.journalApp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUserName(String userName);

    @Query(value = "SELECT * FROM users WHERE FIND_IN_SET(:journalId, journal_entries)", nativeQuery = true)
    Optional<UserEntity> findByUserByJournalId(@Param("journalId") Integer journalId);
}
