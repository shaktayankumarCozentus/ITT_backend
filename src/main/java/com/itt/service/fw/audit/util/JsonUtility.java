package com.itt.service.fw.audit.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JsonUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());     // For LocalDateTime etc.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional, improves readability
        // You can add more modules like JavaTimeModule here if needed
    }

    public static String maskPayload(Object input, Set<String> maskFields) {
        try {
            String json = objectMapper.writeValueAsString(input);
            JsonNode rootNode = objectMapper.readTree(json);
            maskRecursive(rootNode, maskFields);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return "[payload masking failed: " + e.getMessage() + "]";
        }
    }

    private static void maskRecursive(JsonNode node, Set<String> maskFields) {
        if (node == null || maskFields.isEmpty()) return;

        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();

                if (matchesIgnoreCase(key, maskFields)) {
                    objNode.put(key, "****");
                } else {
                    maskRecursive(entry.getValue(), maskFields);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode item : arrayNode) {
                maskRecursive(item, maskFields);
            }
        }
        // primitives don't need masking unless they are inside an object/array
    }

    private static boolean matchesIgnoreCase(String key, Set<String> maskFields) {
        return maskFields.stream().anyMatch(mask -> mask.equalsIgnoreCase(key));
    }
}
