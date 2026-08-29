package com.fittrack.storage.service;

/** An upload request expressed without reference to any provider's SDK. */
public record StorageUpload(
        byte[] content, String contentType, String originalFilename, String folderHint) {}
