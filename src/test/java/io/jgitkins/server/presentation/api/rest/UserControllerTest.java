package io.jgitkins.server.presentation.api.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jgitkins.server.application.dto.result.UserSummary;
import io.jgitkins.server.application.port.in.PublicUserQueryUseCase;
import io.jgitkins.server.application.port.in.UserProfileUpdateUseCase;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicUserQueryUseCase publicUserQueryUseCase;

    @MockBean
    private UserProfileUpdateUseCase userProfileUpdateUseCase;

    @Test
    void listUsers_returnsApiResponseWithUserSummaries() throws Exception {
        List<UserSummary> users = List.of(
                new UserSummary(1L, "alice", "Alice", "https://img/a.png", LocalDateTime.of(2026, 1, 1, 0, 0)),
                new UserSummary(2L, "bob", "Bob", null, LocalDateTime.of(2026, 1, 2, 0, 0)));
        when(publicUserQueryUseCase.getUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].username").value("alice"))
                .andExpect(jsonPath("$.data[1].username").value("bob"));

        verify(publicUserQueryUseCase).getUsers();
    }

    @Test
    void updateUsername_callsUseCaseAndReturnsOk() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("username", "new_name"));

        mockMvc.perform(patch("/api/users/me/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(userProfileUpdateUseCase).updateUsername("new_name");
    }

    @Test
    void updateUsername_withMissingField_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/users/me/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
