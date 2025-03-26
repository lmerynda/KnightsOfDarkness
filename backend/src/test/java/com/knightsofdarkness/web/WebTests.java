package com.knightsofdarkness.web;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ComponentScan(basePackages =
{ "com.knightsofdarkness.*" })
@EntityScan(basePackages =
{ "com.knightsofdarkness.*" })
class WebTests {
    // @Autowired
    // private MockMvc mockMvc;

    // @Disabled
    // @Test
    // void healthTest() throws Exception
    // {
    // this.mockMvc.perform(MockMvcRequestBuilders
    // .get("/health")
    // .contentType(MediaType.APPLICATION_JSON))
    // .andExpect(status().isOk());
    // }
}
