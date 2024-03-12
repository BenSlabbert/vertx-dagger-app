/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

record ParameterSchema(String name, ParamType type, boolean required, In in) {}
