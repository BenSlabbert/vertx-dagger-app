package com.example.catalog.entity;

import java.util.UUID;

public record Item(UUID id, String name, long priceInCents) {}
