package net.friedl.fling.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import net.friedl.fling.model.dto.ArtifactDto;
import net.friedl.fling.service.ArtifactService;

@WebMvcTest(ArtifactController.class)
class ArtifactControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArtifactService artifactService;

    @Test
    void testGetArtifacts_noArtifacts_empty() throws Exception {
        var flingId = 123L;

        when(artifactService.findAllArtifacts(flingId)).thenReturn(List.of());

        mvc.perform(get("/api/fling/{flingId}/artifact", flingId)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetArtifacts_hasArtifacts_allArtifacts() throws Exception {
        var flingId = 123L;
        var artifactName = "TEST";

        ArtifactDto artifactDto = new ArtifactDto();
        artifactDto.setName(artifactName);

        when(artifactService.findAllArtifacts(flingId)).thenReturn(List.of(artifactDto));

        mvc.perform(get("/api/fling/{flingId}/artifact", flingId)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo(artifactName)));
    }
}
