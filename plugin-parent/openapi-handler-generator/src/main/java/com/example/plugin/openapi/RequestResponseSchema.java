/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import java.util.List;
import java.util.Optional;

record RequestResponseSchema(
    String path,
    Method method,
    List<ParameterSchema> parameters,
    Optional<RequestBodySchema> requestBodySchema,
    ResponseSchema responseSchema) {}
