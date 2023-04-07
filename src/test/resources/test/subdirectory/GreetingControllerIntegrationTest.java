package io.enfuse.democrudapp.greeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GreetingRepository greetingRepository;

    @Test
    void post_saves() throws Exception {
        GreetingInfo request = new GreetingInfo(null, "This is my content");

        mockMvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("This is my content"));

    }

    @Test
    void put_update() throws Exception {
        greetingRepository.save(new GreetingRecord(1L, "fantastic content"));

        GreetingInfo request = new GreetingInfo(1L, "This is my updated content");

        mockMvc.perform(put("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("This is my updated content"));

    }

    @Test
    void getAll_retrivesGreetings() throws Exception {
        greetingRepository.save(new GreetingRecord(1L, "fantastic content"));

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("fantastic content"))
        ;
    }

    @Test
    void getOne_retrievesGreeting() throws Exception {
        mockMvc.perform(get("/greetings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_deletes() throws Exception {
        greetingRepository.save(new GreetingRecord(1L, "fantastic content"));

        mockMvc.perform(delete("/greetings/1"))
                .andExpect(status().isOk());

        assertTrue(greetingRepository.findById(1L).isEmpty());
    }

}
