package net.friedl.fling.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.model.dto.FlingDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ModelTestConfiguration.class)
public class FlingDtoTest {

  @Autowired
  private Validator validator;

  @Test
  void testSetId_null_validationFails() {
    FlingDto flingDto = FlingDto.builder()
        .id(null)
        .name("test")
        .creationTime(Instant.EPOCH)
        .shareId("test")
        .build();


    Set<ConstraintViolation<FlingDto>> constraintViolations = validator.validate(flingDto);

    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<FlingDto> violation = constraintViolations.iterator().next();
    assertThat(violation.getPropertyPath().toString()).isEqualTo("id");
    assertThat(violation.getMessage()).isEqualTo("must not be null");
  }

  @Test
  void testSetName_null_validationFails() {
    FlingDto flingDto = FlingDto.builder()
        .id(new UUID(0L, 0L))
        .name(null)
        .creationTime(Instant.EPOCH)
        .shareId("test")
        .build();


    Set<ConstraintViolation<FlingDto>> constraintViolations = validator.validate(flingDto);

    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<FlingDto> violation = constraintViolations.iterator().next();
    assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    assertThat(violation.getMessage()).isEqualTo("must not be null");
  }

  @Test
  void testSetCreationTime_null_validationFails() {
    FlingDto flingDto = FlingDto.builder()
        .id(new UUID(0L, 0L))
        .name("test")
        .creationTime(null)
        .shareId("test")
        .build();


    Set<ConstraintViolation<FlingDto>> constraintViolations = validator.validate(flingDto);

    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<FlingDto> violation = constraintViolations.iterator().next();
    assertThat(violation.getPropertyPath().toString()).isEqualTo("creationTime");
    assertThat(violation.getMessage()).isEqualTo("must not be null");
  }


  @Test
  void testSetShareId_null_validationOk() { // must be nullable to support defaulting in service
    FlingDto flingDto = FlingDto.builder()
        .id(new UUID(0L, 0L))
        .name("test")
        .creationTime(Instant.EPOCH)
        .shareId(null)
        .build();


    Set<ConstraintViolation<FlingDto>> constraintViolations = validator.validate(flingDto);

    assertThat(constraintViolations).hasSize(0);
  }

  @Test
  void testSetAllManadatory_validationOk() {
    FlingDto flingDto = FlingDto.builder()
        .id(new UUID(0L, 0L))
        .name("test")
        .creationTime(Instant.EPOCH)
        .shareId("test")
        .build();


    Set<ConstraintViolation<FlingDto>> constraintViolations = validator.validate(flingDto);
    assertTrue(constraintViolations.isEmpty());
  }
}
