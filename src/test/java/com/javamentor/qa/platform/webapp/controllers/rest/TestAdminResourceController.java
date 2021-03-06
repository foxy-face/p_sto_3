package com.javamentor.qa.platform.webapp.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.javamentor.qa.platform.models.dto.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAdminResourceController extends AbstractControllerTest {

    @Test
    @DataSet(value = {
            "dataset/adminResourceController/roles.yml",
            "dataset/adminResourceController/users.yml",
    }
    , disableConstraints = true
    )
    public void deleteUserById() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setPassword("ADMIN");
        authenticationRequest.setUsername("admin@mail.ru");

        String USER_TOKEN = getToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        mockMvc.perform(
                        delete("/api/admin/delete/100")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DataSet(value = {
            "dataset/adminResourceController/roles.yml",
            "dataset/adminResourceController/users.yml",
    }
            , disableConstraints = true
    )
    public void deleteUserByIdNotFound() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setPassword("ADMIN");
        authenticationRequest.setUsername("admin@mail.ru");

        String USER_TOKEN = getToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        mockMvc.perform(
                        delete("/api/admin/delete/155")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DataSet(value = {
            "dataset/adminResourceController/roles.yml",
            "dataset/adminResourceController/users.yml",
            "dataset/UserResourceController/reputations.yml",
    }
            , disableConstraints = true
    )
    public void getUserByIdForbidden() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setPassword("ADMIN");
        authenticationRequest.setUsername("admin@mail.ru");

        String USER_TOKEN = getToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        mockMvc.perform(
                        get("/api/user/100")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/admin/delete/100")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/user/100")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = {
            "dataset/adminResourceController/testRoleUserAccess/roles.yml",
            "dataset/adminResourceController/testRoleUserAccess/users.yml",
    }
            , disableConstraints = true
    )
    public void testRoleUserAccess() throws Exception {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setPassword("USER");
        authenticationRequest.setUsername("user@mail.ru");

        String USER_TOKEN = getToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        mockMvc.perform(
                        delete("/api/admin/delete/100")
                                .header(AUTHORIZATION, USER_TOKEN))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

}
