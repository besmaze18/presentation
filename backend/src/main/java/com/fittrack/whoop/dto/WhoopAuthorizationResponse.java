package com.fittrack.whoop.dto;

/** The URL the browser should be sent to in order to authorise WHOOP access. */
public record WhoopAuthorizationResponse(String authorizationUrl) {}
