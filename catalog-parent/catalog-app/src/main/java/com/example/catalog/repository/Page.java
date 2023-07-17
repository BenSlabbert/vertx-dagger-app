package com.example.catalog.repository;

import java.util.List;

public record Page<T>(boolean more, long total, List<T> items) {}
