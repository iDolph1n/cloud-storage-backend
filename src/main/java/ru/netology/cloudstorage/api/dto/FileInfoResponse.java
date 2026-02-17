package ru.netology.cloudstorage.api.dto;

public record FileInfoResponse(
    String filename,
    long size
) {}
