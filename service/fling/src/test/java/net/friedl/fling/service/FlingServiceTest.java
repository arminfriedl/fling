package net.friedl.fling.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import net.friedl.fling.model.dto.FlingDto;
import net.friedl.fling.model.mapper.FlingMapper;
import net.friedl.fling.model.mapper.FlingMapperImpl;
import net.friedl.fling.persistence.entities.FlingEntity;
import net.friedl.fling.persistence.repositories.FlingRepository;
import net.friedl.fling.service.archive.ArchiveService;

@ExtendWith(SpringExtension.class)
public class FlingServiceTest {
  @Autowired
  private FlingService flingService;
  
  @Autowired
  private FlingMapper flingMapper;

  @Autowired
  private MessageDigest keyHashDigest;

  @MockBean
  private FlingRepository flingRepository;

  @MockBean
  private ArchiveService archiveService;

  private FlingEntity flingEntity1;

  private FlingEntity flingEntity2;

  @TestConfiguration
  static class FlingServiceTestConfiguration {
    @Bean
    public FlingMapper flingMapper() {
      return new FlingMapperImpl();
    }
    
    @Bean
    public MessageDigest keyHashDigest() throws NoSuchAlgorithmException {
      return MessageDigest.getInstance("SHA-512");
    }

    @Bean
    public FlingService flingService(FlingRepository flingRepository, FlingMapper flingMapper,
        ArchiveService archiveService, MessageDigest keyHashDigest) {
      return new FlingService(flingRepository, flingMapper, archiveService, keyHashDigest);
    }
  }

  @BeforeEach
  public void beforeEach() {
    this.flingEntity1 = new FlingEntity();
    flingEntity1.setId(UUID.randomUUID());
    flingEntity1.setName("fling1");
    flingEntity1.setAuthCode(new String(Hex.encode(keyHashDigest.digest("authCode1".getBytes()))));
    flingEntity1.setCreationTime(Instant.now());

    this.flingEntity2 = new FlingEntity();
    flingEntity2.setId(UUID.randomUUID());
    flingEntity2.setName("fling2");
    flingEntity2.setShareId("shareId2");
    flingEntity2.setCreationTime(Instant.now());

    when(flingRepository.save(any())).then(new Answer<FlingEntity>() {
      @Override
      public FlingEntity answer(InvocationOnMock invocation) throws Throwable {
        FlingEntity flingEntity = invocation.getArgument(0);
        flingEntity.setId(UUID.randomUUID());
        return flingEntity;
      }});
  }

  @Test
  public void findAll_noFlings_empty() {
    when(flingRepository.findAll()).thenReturn(List.of());

    assertThat(flingService.findAll(), is(empty()));
  }

  @Test
  public void findAll_hasFlings_allFlings() {
    when(flingRepository.findAll()).thenReturn(List.of(flingEntity1, flingEntity2));

    assertThat(flingService.findAll(), hasItems(
        flingMapper.map(flingEntity1), flingMapper.map(flingEntity2)));
  }

  @Test
  public void getById_flingDto() {
    when(flingRepository.getOne(flingEntity1.getId())).thenReturn(flingEntity1);
    assertThat(flingService.getById(flingEntity1.getId()), equalTo(flingMapper.map(flingEntity1)));
  }

  @Test
  public void create_emptyFling_defaultValues() {
    FlingDto flingDto = new FlingDto();

    FlingDto createdFling = flingService.create(flingDto);
    assertThat(createdFling.getShareId(), not(emptyOrNullString()));
    assertThat(createdFling.getAuthCode(), emptyOrNullString());
  }

  @Test
  public void create_hasAuthCode_setAuthCode() {
    FlingDto flingDto = new FlingDto();
    flingDto.setAuthCode("test");
    
    String hashedAuthCode = new String(Hex.encode(keyHashDigest.digest(flingDto.getAuthCode().getBytes())));
    
    FlingDto createdFling = flingService.create(flingDto);
    assertThat(createdFling.getAuthCode(), is(hashedAuthCode));
  }

  @Test
  public void create_hasShareId_setShareId() {
    FlingDto flingDto = new FlingDto();
    flingDto.setShareId("test");

    FlingDto createdFling = flingService.create(flingDto);
    assertThat(createdFling.getShareId(), is("test"));
  }
  
  @Test
  public void getByShareId_flingDto() {
    when(flingRepository.findByShareId("shareId2")).thenReturn(flingEntity2);
    
    FlingDto foundFling = flingService.getByShareId("shareId2");
    assertThat(foundFling.getShareId(), equalTo("shareId2"));
  }
  
  @Test
  public void delete_deletesFromArchiveAndDb() throws IOException {
    UUID testId = UUID.randomUUID();
    flingService.delete(testId);
    
    verify(archiveService).deleteFling(testId);
    verify(flingRepository).deleteById(testId);
  }
  
  @Test
  public void validateAuthCode_codesMatch_true() {
    when(flingRepository.getOne(flingEntity1.getId())).thenReturn(flingEntity1);
   
    assertThat(flingService.validateAuthCode(flingEntity1.getId(), "authCode1"), is(true));
  }
  
  @Test
  public void validateAuthCode_codesDoNotMatch_false() {
     when(flingRepository.getOne(flingEntity2.getId())).thenReturn(flingEntity2);
   
    assertThat(flingService.validateAuthCode(flingEntity2.getId(), "authCode1"), is(false));
  }

}
