package com.example.bookcatalog.book;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Fast unit-level tests of the web layer. The SoapUI suite in /soapui covers the
 * same API end-to-end against the running jar.
 */
@WebMvcTest(BookController.class)
@Import(BookRepository.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Dune","author":"Frank Herbert","isbn":"9780441013593","publishedYear":1965}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title", is("Dune")));
    }

    @Test
    void rejectsInvalidBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Nobody","publishedYear":3000}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404ForUnknownBook() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 424242))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
