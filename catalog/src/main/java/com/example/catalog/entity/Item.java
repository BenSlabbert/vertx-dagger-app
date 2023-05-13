package com.example.catalog.entity;

import java.util.UUID;

public record Item(long id, UUID uuid, String name, long priceInCents) {}
