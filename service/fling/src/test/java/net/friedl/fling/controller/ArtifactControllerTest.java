package net.friedl.fling.controller;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(controllers = ArtifactController.class,
    // do auto-configure security
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    // do not try to create beans in security
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = "net.friedl.fling.security.*"))
class ArtifactControllerTest {
//  @Autowired
//  private MockMvc mvc;
//
//  @MockBean
//  private ArtifactService artifactService;
//
//  @Test
//  public void testGetArtifacts_noArtifacts_empty() throws Exception {
////    Long flingId = 123L;
////
////    when(artifactService.findAllArtifacts(flingId)).thenReturn(List.of());
////
////    mvc.perform(get("/api/artifacts").param("flingId", flingId.toString()))
////        .andExpect(jsonPath("$", hasSize(0)));
//  }
//
//  @Test
//  public void testGetArtifacts_hasArtifacts_allArtifacts() throws Exception {
////    Long flingId = 123L;
////    String artifactName = "TEST";
////
////    ArtifactDto artifactDto = new ArtifactDto();
////    artifactDto.setName(artifactName);
////
////    when(artifactService.findAllArtifacts(flingId)).thenReturn(List.of(artifactDto));
////
////    mvc.perform(get("/api/artifacts").param("flingId", flingId.toString()))
////        .andExpect(jsonPath("$", hasSize(1)))
////        .andExpect(jsonPath("$[0].name", equalTo(artifactName)));
//  }
}
