/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import com.example.plugin.openapi.type.SchemaType;

record ResponseSchema(
    int statusCode, boolean empty, SchemaType schemaType, ObjectSchema objectSchema) {}
