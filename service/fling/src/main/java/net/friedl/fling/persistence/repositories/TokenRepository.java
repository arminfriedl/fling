package net.friedl.fling.persistence.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import net.friedl.fling.persistence.entities.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, UUID> {
}
