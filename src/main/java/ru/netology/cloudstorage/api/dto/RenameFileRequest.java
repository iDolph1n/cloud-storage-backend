package ru.netology.cloudstorage.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameFileRequest(
        @NotBlank String filename
) {}
