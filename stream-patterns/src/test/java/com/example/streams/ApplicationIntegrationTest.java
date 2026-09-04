package com.example.streams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationIntegrationTest {
 @Autowired TestRestTemplate client;
 @Test void realHttpServerConnectsAllLayers() {
  var response=client.getForEntity("/api/items/inventory",String.class);
  assertThat(response.getStatusCode().value()).isEqualTo(200);
  assertThat(response.getBody()).contains("4800.00","1500.00","50.00");
 }
}
