/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.plugin.openapi.type.Method;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void test() {
    String string = StringUtils.variableName("post-id");
    assertThat(string).isEqualTo("postId");
  }

  @Test
  void capitalizeFirstChar_ShouldCapitalizeFirstCharacter_WhenInputIsValid() {
    String result = StringUtils.capitalizeFirstChar("hello");
    assertThat(result).isEqualTo("Hello");
  }

  @Test
  void capitalizeFirstChar_ShouldThrowException_WhenInputIsNull() {
    assertThatThrownBy(() -> StringUtils.capitalizeFirstChar(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("input string cannot be null or empty");
  }

  @Test
  void capitalizeFirstChar_ShouldThrowException_WhenInputIsEmpty() {
    assertThatThrownBy(() -> StringUtils.capitalizeFirstChar(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("input string cannot be null or empty");
  }

  @Test
  void getParameterParserClassName_ShouldReturnCorrectClassName_WhenInputIsValid() {
    String result = StringUtils.getParameterParserClassName(Method.GET, "/users/{id}");
    assertThat(result).isEqualTo("GetUsersByIdParameterParser");
  }

  @Test
  void getParameterParserClassName_ShouldReturnCorrectClassName_WhenInputIsValid_specialChars() {
    String result = StringUtils.getParameterParserClassName(Method.GET, "/users/{post-id}");
    assertThat(result).isEqualTo("GetUsersByPostIdParameterParser");
  }

  @Test
  void getParameterParserClassName_ShouldReturnCorrectClassName_WhenPathHasNoParameters() {
    String result = StringUtils.getParameterParserClassName(Method.GET, "/users");
    assertThat(result).isEqualTo("GetUsersParameterParser");
  }

  @Test
  void getParameterParserClassName_ShouldReturnCorrectClassName_WhenPathIsEmpty() {
    String result = StringUtils.getParameterParserClassName(Method.GET, "");
    assertThat(result).isEqualTo("GetParameterParser");
  }
}
