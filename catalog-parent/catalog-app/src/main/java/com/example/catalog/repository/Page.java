/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import java.util.List;

public record Page<T>(boolean more, long total, List<T> items) {}
