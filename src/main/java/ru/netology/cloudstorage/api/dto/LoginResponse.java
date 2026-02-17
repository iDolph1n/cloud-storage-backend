package ru.netology.cloudstorage.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("auth-token") String authToken
) {}
