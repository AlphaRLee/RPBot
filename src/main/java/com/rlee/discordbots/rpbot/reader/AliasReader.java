package com.rlee.discordbots.rpbot.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rlee.discordbots.rpbot.regitstry.AliasRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Reads in aliases from game config file
 */
public class AliasReader {
    private final AliasRegistry aliasRegistry;

    public AliasReader(AliasRegistry aliasRegistry) {
        this.aliasRegistry = aliasRegistry;
    }

    public void readAliasesFromFile(String configFilePath) throws FileNotFoundException {
        File configFile = new File(configFilePath);
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        if (!configFile.exists()) {
            throw new FileNotFoundException("Config file not found!"); // TODO ensure that a config file always exists
        }

        try {
            ObjectNode aliasesNode = (ObjectNode) yamlMapper.readTree(configFile).get("aliases");
            if (aliasesNode == null) {
                System.out.println("Game has no aliases in config file, skipping reading aliases");
                return;
            }

            for (Iterator<Map.Entry<String, JsonNode>> it = aliasesNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> aliasData = it.next();
                String fullName = yamlMapper.treeToValue(aliasData.getValue(), String.class);
                aliasRegistry.setAlias(aliasData.getKey(), fullName);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
