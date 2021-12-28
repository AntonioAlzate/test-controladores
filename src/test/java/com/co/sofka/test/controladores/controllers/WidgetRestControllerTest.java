package com.co.sofka.test.controladores.controllers;

import com.co.sofka.test.controladores.models.Widget;
import com.co.sofka.test.controladores.services.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /widgets success")
    void testGetWidgetsSuccess() throws Exception {
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
        doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }

    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception {
        doReturn(Optional.empty()).when(service).findById(1l);

        mockMvc.perform(get("/rest/widget/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /rest/widget/1 - EXIST")
    void testGetWidgetByIdExist() throws Exception {
        Widget widgetToReturn = new Widget(1L, "Widget Name", "Description", 1);
        doReturn(Optional.of(widgetToReturn)).when(service).findById(1l);

        mockMvc.perform(get("/rest/widget/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Widget Name")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.version", is(1)));

    }

    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception {
        Widget widgetToPost = new Widget("New Widget", "This is my widget");
        Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
        doReturn(widgetToReturn).when(service).save(any());

        mockMvc.perform(post("/rest/widget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetToPost)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("PUT /rest/proper/widget/{id}")
    void testUpdateWidget() throws Exception {
        Widget widgetToUpdate = new Widget(1L, "Widget update", "This is my widget update", 1);
        Widget widgetToReturn = new Widget(1L, "Widget update", "This is my widget update", 2);

        doReturn(Optional.of(widgetToUpdate)).when(service).findById(1L);
        doReturn(widgetToReturn).when(service).save(any());

        mockMvc.perform(put("/rest/proper/widget/{id}", 1L)
                        .header("If-Match", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(widgetToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Widget update")))
                .andExpect(jsonPath("$.description", is("This is my widget update")))
                .andExpect(jsonPath("$.version", is(2)));
    }

    @Test
    @DisplayName("Put /rest/proper/widget/1 - Not Found")
    void testPutWidgetByIdNotFound() throws Exception {

        Widget widgetToUpdate = new Widget(1L, "Widget update", "This is my widget update", 1);

        doReturn(Optional.empty()).when(service).findById(1l);

        mockMvc.perform(put("/rest/proper/widget/{id}", 1L)
                .header("If-Match", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToUpdate)))
                .andExpect(status().isNotFound());
    }

    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
