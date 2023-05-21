package com.example.commons.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Test;

class ParseConfigTest {

  @Test
  void test() throws IOException {
    URL resource = ParseConfigTest.class.getClassLoader().getResource("config.json");
    System.err.println("resource: " + resource);
    String string = resource.getPath().toString();
    Config config = ParseConfig.parseArgs(new String[] {string});
    assertNotNull(config);
  }
}
