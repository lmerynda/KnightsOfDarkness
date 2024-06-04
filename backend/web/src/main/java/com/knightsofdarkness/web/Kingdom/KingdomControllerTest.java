import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

class KingdomControllerTest {
    private MockMvc mockMvc;

    @Mock
    private KingdomService kingdomService;

    private KingdomController kingdomController;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        kingdomController = new KingdomController(kingdomService);
        mockMvc = MockMvcBuilders.standaloneSetup(kingdomController)
                .setControllerAdvice(new KingdomControllerExceptionHandler())
                .build();
    }

    @Test
    void createKingdom_shouldReturnCreatedStatusAndLocationHeader() throws Exception
    {
        // Arrange
        KingdomDto kingdomDto = new KingdomDto();
        kingdomDto.setName("Test Kingdom");

        KingdomDto createdKingdomDto = new KingdomDto();
        createdKingdomDto.setName("Test Kingdom");
        createdKingdomDto.setId(1L);

        when(kingdomService.createKingdom(any(KingdomDto.class))).thenReturn(createdKingdomDto);

        // Act
        MvcResult mvcResult = mockMvc.perform(post("/kingdom/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(kingdomDto)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        String locationHeader = mvcResult.getResponse().getHeader("Location");
        URI expectedLocation = UriComponentsBuilder.fromPath("/kingdom/{name}")
                .buildAndExpand(createdKingdomDto.getName())
                .toUri();
        assert locationHeader != null;
        assert locationHeader.equals(expectedLocation.toString());
    }

    @Test
    void createKingdom_whenDataIntegrityViolationExceptionThrown_shouldReturnConflictStatus() throws Exception
    {
        // Arrange
        KingdomDto kingdomDto = new KingdomDto();
        kingdomDto.setName("Test Kingdom");

        when(kingdomService.createKingdom(any(KingdomDto.class)))
                .thenThrow(new DataIntegrityViolationException("Kingdom already exists"));

        // Act & Assert
        mockMvc.perform(post("/kingdom/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(kingdomDto)))
                .andExpect(status().isConflict());
    }

    private String asJsonString(Object obj) throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}