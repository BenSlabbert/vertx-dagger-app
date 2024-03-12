/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

record ResponseSchema(
    int statusCode, boolean empty, SchemaType schemaType, ObjectSchema objectSchema) {}
