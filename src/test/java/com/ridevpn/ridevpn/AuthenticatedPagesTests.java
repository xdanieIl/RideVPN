package com.ridevpn.ridevpn;

import com.ridevpn.ridevpn.repository.UserRepository;
import com.ridevpn.ridevpn.service.UserService;
import com.ridevpn.ridevpn.service.WireGuardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthenticatedPagesTests {
    @Autowired WebApplicationContext context;
    @Autowired UserService users;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder encoder;
    @MockitoBean WireGuardService wireGuard;
    MockMvc mvc;

    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        repository.deleteAll();
        users.register("rider", "rider@example.test", "original-password");
    }

    MockHttpSession signIn() throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("username", "rider").param("password", "original-password"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard"))
            .andReturn().getRequest().getSession(false);
    }

    @Test void guestsCannotOpenAppPages() throws Exception {
        for (String page : new String[]{"/dashboard", "/settings", "/account"}) {
            mvc.perform(get(page).accept("text/html")).andExpect(status().is3xxRedirection());
        }
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test void loginCreatesSessionAndPagesRenderRealAccount() throws Exception {
        MockHttpSession session = signIn();
        for (String page : new String[]{"/dashboard", "/settings", "/account"}) {
            mvc.perform(get(page).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        }
        mvc.perform(get("/account").session(session)).andExpect(content().string(containsString("rider@example.test")));
        mvc.perform(get("/").session(session)).andExpect(redirectedUrl("/dashboard"));
        mvc.perform(post("/logout").session(session).with(csrf()).accept("text/html")).andExpect(redirectedUrl("/?logout"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test void invalidLoginAndMissingCsrfAreRejected() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "rider").param("password", "wrong"))
            .andExpect(redirectedUrl("/?error"));
        mvc.perform(post("/login").param("username", "rider").param("password", "original-password"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/account/email").session(signIn()).param("email", "changed@example.test"))
            .andExpect(status().isForbidden());
        assertThat(repository.findByUsername("rider").orElseThrow().getEmail()).isEqualTo("rider@example.test");
    }

    @Test void emailAndPasswordUpdatesPersistAndValidate() throws Exception {
        MockHttpSession session = signIn();
        mvc.perform(post("/account/email").session(session).with(csrf()).param("email", "changed@example.test"))
            .andExpect(redirectedUrl("/account")).andExpect(flash().attribute("message", "Email updated."));
        assertThat(repository.findByUsername("rider").orElseThrow().getEmail()).isEqualTo("changed@example.test");
        mvc.perform(post("/account/email").session(session).with(csrf()).param("email", "invalid"))
            .andExpect(flash().attributeExists("error"));
        mvc.perform(post("/account/password").session(session).with(csrf())
                .param("currentPassword", "wrong").param("newPassword", "updated-password").param("confirmPassword", "updated-password"))
            .andExpect(flash().attributeExists("error"));
        assertThat(encoder.matches("original-password", repository.findByUsername("rider").orElseThrow().getPasswordHash())).isTrue();
        mvc.perform(post("/account/password").session(session).with(csrf())
                .param("currentPassword", "original-password").param("newPassword", "updated-password").param("confirmPassword", "updated-password"))
            .andExpect(flash().attribute("message", "Password updated."));
        assertThat(encoder.matches("updated-password", repository.findByUsername("rider").orElseThrow().getPasswordHash())).isTrue();
    }

    @Test void registrationStillWorksWithCsrf() throws Exception {
        mvc.perform(post("/register").with(csrf()).param("username", "newrider")
                .param("email", "new@example.test").param("password", "new-rider-password"))
            .andExpect(redirectedUrl("/?registered"));
        assertThat(users.checkLogin("newrider", "new-rider-password")).isTrue();
    }

    @Test void configDownloadRequiresPostAndCsrfAndReturnsAttachment() throws Exception {
        MockHttpSession session = signIn();
        mvc.perform(get("/api/config").session(session)).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/config").session(session)).andExpect(status().isForbidden());
        verifyNoInteractions(wireGuard);
        when(wireGuard.createConfig("rider")).thenReturn("[Interface]\nAddress = 10.200.0.2/32\n");
        mvc.perform(post("/api/config").session(session).with(csrf()))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"ridevpn.conf\""));
        verify(wireGuard).createConfig("rider");
    }

    @Test void configFailureDoesNotLeakServerDetails() throws Exception {
        when(wireGuard.createConfig("rider")).thenThrow(new java.io.IOException("private server details"));
        mvc.perform(post("/api/config").session(signIn()).with(csrf()))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().string("Could not generate your VPN config. Please try again later."));
    }
}
