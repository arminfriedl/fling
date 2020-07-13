package net.friedl.fling.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.model.dto.ArtifactDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ModelTestConfiguration.class)
public class ArtifactDtoTest {

  @Autowired
  private Validator validator;

  @Test
  void testSetId_null_validationOk() {
    ArtifactDto artifactDto = ArtifactDto.builder()
        .id(null)
        .path(Paths.get("test"))
        .build();


    Set<ConstraintViolation<ArtifactDto>> constraintViolations = validator.validate(artifactDto);

    assertThat(constraintViolations).hasSize(0);
  }

  @Test
  void testSetPath_null_validationFails() {
    ArtifactDto artifactDto = ArtifactDto.builder()
        .id(new UUID(0L, 0L))
        .path(null)
        .build();


    Set<ConstraintViolation<ArtifactDto>> constraintViolations = validator.validate(artifactDto);

    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<ArtifactDto> violation = constraintViolations.iterator().next();
    assertThat(violation.getPropertyPath().toString()).isEqualTo("path");
    assertThat(violation.getMessage()).isEqualTo("must not be null");
  }

  @Test
  void testMandatoryFieldsSet_validationOk() {
    ArtifactDto artifactDto = ArtifactDto.builder()
        .id(new UUID(0L, 0L))
        .path(Paths.get("test"))
        .build();

    Set<ConstraintViolation<ArtifactDto>> constraintViolations = validator.validate(artifactDto);
    assertTrue(constraintViolations.isEmpty());
  }

}
