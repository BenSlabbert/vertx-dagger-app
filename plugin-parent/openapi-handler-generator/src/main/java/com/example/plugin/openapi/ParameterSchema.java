/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import com.example.plugin.openapi.type.In;
import com.example.plugin.openapi.type.ParamType;

record ParameterSchema(String name, ParamType type, boolean required, In in) {}
