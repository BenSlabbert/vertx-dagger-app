/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import com.example.plugin.openapi.type.ParamType;
import java.util.List;

record ObjectSchema(String name, List<Property> properties) {
  record Property(String name, ParamType type, boolean required) {}
}
