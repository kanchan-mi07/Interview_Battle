package com.interviewarena;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewarena.entity.OptionChoice;
import com.interviewarena.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BattleFlowIntegrationTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    QuestionRepository questions;
    @Autowired
    JdbcTemplate jdbc;

    // ------------------------------------------------------------ tests

    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        doGet("/api/users/me", null).andExpect(status().isUnauthorized());
        doGet("/api/leaderboard", null).andExpect(status().isUnauthorized());
        doPost("/api/battles", null, null).andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndDuplicateRules() throws Exception {
        String name = uniqueName();
        register(name);

        // same email again -> 409
        doPost("/api/auth/register", null,
                Map.of("username", name + "x", "email", name + "@test.com", "password", PASSWORD))
                .andExpect(status().isConflict());

        // wrong password -> 401, same message as an unknown email
        doPost("/api/auth/login", null, Map.of("email", name + "@test.com", "password", "wrong-password"))
                .andExpect(status().isUnauthorized());

        String token = login(name);
        JsonNode me = json(doGet("/api/users/me", token).andExpect(status().isOk()));
        assertEquals(name, me.get("username").asText());
        assertEquals(1000, me.get("rating").asInt());
        assertFalse(me.has("password"), "password hash must never be returned");
    }

    @Test
    void usernameLengthIsCheckedAfterTrimming() throws Exception {
        doPost("/api/auth/register", null,
                Map.of("username", "  ab  ", "email", uniqueName() + "@test.com", "password", PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotJoinOwnBattle() throws Exception {
        String token = registerAndLogin(uniqueName());
        JsonNode battle = json(doPost("/api/battles", token, null).andExpect(status().isCreated()));
        doPost("/api/battles/join", token, Map.of("battleCode", battle.get("battleCode").asText()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullBattleUpdatesScoresAndRatings() throws Exception {
        String creatorName = uniqueName(), opponentName = uniqueName();
        String creator = registerAndLogin(creatorName);
        String opponent = registerAndLogin(opponentName);

        long battleId = startBattle(creator, opponent);

        // creator answers everything correctly, opponent answers everything wrong
        JsonNode creatorFinal = playAll(battleId, creator, true);
        assertEquals(1000, creatorFinal.get("myScore").asInt());
        JsonNode opponentFinal = playAll(battleId, opponent, false);

        assertEquals("COMPLETED", opponentFinal.get("status").asText());
        assertEquals("LOSS", opponentFinal.get("result").get("outcome").asText());

        JsonNode asCreator = json(doGet("/api/battles/" + battleId, creator).andExpect(status().isOk()));
        assertEquals("COMPLETED", asCreator.get("status").asText());
        assertEquals("WIN", asCreator.get("result").get("outcome").asText());
        assertEquals(1000, asCreator.get("result").get("ratingBefore").asInt());
        assertEquals(1025, asCreator.get("result").get("ratingAfter").asInt());

        assertEquals(1025, json(doGet("/api/users/me", creator).andExpect(status().isOk())).get("rating").asInt());
        assertEquals(985, json(doGet("/api/users/me", opponent).andExpect(status().isOk())).get("rating").asInt());

        // answering a finished battle is rejected
        doPost("/api/battles/" + battleId + "/answer", creator,
                Map.of("questionId", 1, "selectedOption", "A")).andExpect(status().isConflict());
    }

    @Test
    void outsiderCannotReadOrAnswerABattle() throws Exception {
        String creator = registerAndLogin(uniqueName());
        String opponent = registerAndLogin(uniqueName());
        String outsider = registerAndLogin(uniqueName());
        long battleId = startBattle(creator, opponent);

        doGet("/api/battles/" + battleId, outsider).andExpect(status().isForbidden());
        doPost("/api/battles/" + battleId + "/answer", outsider,
                Map.of("questionId", 1, "selectedOption", "A")).andExpect(status().isForbidden());
    }

    @Test
    void practiceEndpointDoesNotLeakAnswersOfALiveBattle() throws Exception {
        String creator = registerAndLogin(uniqueName());
        String opponent = registerAndLogin(uniqueName());
        long battleId = startBattle(creator, opponent);

        JsonNode state = json(doGet("/api/battles/" + battleId, creator).andExpect(status().isOk()));
        long currentQuestionId = state.get("currentQuestion").get("id").asLong();

        // The cheat: ask the practice endpoint for the answer to the question currently on screen.
        doPost("/api/questions/practice/submit", creator,
                Map.of("answers", List.of(Map.of("questionId", currentQuestionId, "selectedOption", "A"))))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------ flow helpers

    /** Creator creates, opponent joins, then the 8 second countdown is skipped. */
    private long startBattle(String creatorToken, String opponentToken) throws Exception {
        JsonNode created = json(doPost("/api/battles", creatorToken, null).andExpect(status().isCreated()));
        long battleId = created.get("id").asLong();
        doPost("/api/battles/join", opponentToken, Map.of("battleCode", created.get("battleCode").asText()))
                .andExpect(status().isOk());
        jdbc.update("update battles set started_at = ? where id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusSeconds(1)), battleId);
        return battleId;
    }

    /** Answers every question for one player and returns the last battle state. */
    private JsonNode playAll(long battleId, String token, boolean correct) throws Exception {
        JsonNode state = json(doGet("/api/battles/" + battleId, token).andExpect(status().isOk()));
        int total = state.get("totalQuestions").asInt();
        for (int i = 0; i < total; i++) {
            long questionId = state.get("currentQuestion").get("id").asLong();
            OptionChoice right = questions.findById(questionId).orElseThrow().getCorrectOption();
            OptionChoice pick = correct ? right : (right == OptionChoice.A ? OptionChoice.B : OptionChoice.A);

            JsonNode response = json(doPost("/api/battles/" + battleId + "/answer", token,
                    Map.of("questionId", questionId, "selectedOption", pick.name()))
                    .andExpect(status().isOk()));
            assertEquals(correct, response.get("correct").asBoolean());
            state = response.get("state");
        }
        return state;
    }

    // ------------------------------------------------------------ user helpers

    private String uniqueName() {
        return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void register(String name) throws Exception {
        doPost("/api/auth/register", null,
                Map.of("username", name, "email", name + "@test.com", "password", PASSWORD))
                .andExpect(status().isCreated());
    }

    private String login(String name) throws Exception {
        JsonNode body = json(doPost("/api/auth/login", null,
                Map.of("email", name + "@test.com", "password", PASSWORD)).andExpect(status().isOk()));
        return body.get("token").asText();
    }

    private String registerAndLogin(String name) throws Exception {
        register(name);
        return login(name);
    }

    // ------------------------------------------------------------ http helpers

    private ResultActions doPost(String url, String token, Object body) throws Exception {
        var req = post(url);
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
        return mvc.perform(req);
    }

    private ResultActions doGet(String url, String token) throws Exception {
        var req = get(url);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    private JsonNode json(ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsString());
    }
}