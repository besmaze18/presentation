package com.fittrack.whoop.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** One page of a WHOOP collection endpoint: {@code {"records": [...], "next_token": "..."}}. */
public record WhoopPage(List<JsonNode> records, String nextToken) {

    public boolean hasMore() {
        return nextToken != null && !nextToken.isBlank();
    }
}
