package io.jgitkins.server.presentation.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jgitkins.server.application.dto.result.UserCredentialIssueResult;
import io.jgitkins.server.application.dto.result.UserCredentialSummary;
import io.jgitkins.server.application.port.in.UserCredentialIssueUseCase;
import io.jgitkins.server.application.port.in.UserCredentialQueryUseCase;
import io.jgitkins.server.application.port.in.UserCredentialRevokeUseCase;
import io.jgitkins.server.presentation.dto.UserCredentialIssueRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserCredentialControllerTest {

    @Mock
    private UserCredentialIssueUseCase userCredentialIssueUseCase;

    @Mock
    private UserCredentialQueryUseCase userCredentialQueryUseCase;

    @Mock
    private UserCredentialRevokeUseCase userCredentialRevokeUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserCredentialController controller = new UserCredentialController(
                userCredentialIssueUseCase,
                userCredentialQueryUseCase,
                userCredentialRevokeUseCase
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void issuePat_returnsCreated() throws Exception {
        UserCredentialIssueRequest request = new UserCredentialIssueRequest();
        request.setName("my-pat");
        request.setDescription("for ci");
        request.setExpiration("2026-12-31");

        when(userCredentialIssueUseCase.issueToken(any()))
                .thenReturn(new UserCredentialIssueResult(10L, "token-value"));

        mockMvc.perform(post("/api/auth/pats")
                        .principal(principal("7"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/auth/pats/10")))
                .andExpect(jsonPath("$.data.credentialId").value(10L))
                .andExpect(jsonPath("$.data.token").value("token-value"));

        verify(userCredentialIssueUseCase).issueToken(any());
    }

    @Test
    void getPatList_returnsList() throws Exception {
        when(userCredentialQueryUseCase.getPatList(7L)).thenReturn(List.of(
                new UserCredentialSummary(1L, "PAT", "my-pat", "desc", LocalDateTime.now(), LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/auth/pats").principal(principal("7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("my-pat"));

        verify(userCredentialQueryUseCase).getPatList(7L);
    }

    @Test
    void revokePat_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/auth/pats/15").principal(principal("7")))
                .andExpect(status().isNoContent());

        verify(userCredentialRevokeUseCase).revokePat(7L, 15L);
    }

    private Authentication principal(String name) {
        return new UsernamePasswordAuthenticationToken(name, "N/A");
    }
}
