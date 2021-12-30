package com.rlee.discordbots.rpbot.dice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A general class for holding configuration for rolling based per RPGame
 */
public class RollConfig {
    private final String configFilePath;

    private boolean rollAttribute;

    /**
     * Create a new RollConfig with default settings
     * @param configFilePath
     */
    public RollConfig(String configFilePath) {
        this.configFilePath = configFilePath;

        rollAttribute = false;
    }

    public boolean getRollAttribute() {
        return rollAttribute;
    }

    /**
     * Read config from the config file's "roll" section
     * @throws FileNotFoundException
     */
    public void readConfigFromFile() throws FileNotFoundException {
        File configFile = new File(configFilePath);
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        if (!configFile.exists()) {
            throw new FileNotFoundException("Config file not found!"); // TODO ensure that a config file always exists
        }

        try {
            ObjectNode node = (ObjectNode) yamlMapper.readTree(configFile).get("roll");
            rollAttribute = yamlMapper.treeToValue(node.get("rollAttribute"), Boolean.class);
        } catch (JsonProcessingException e) {
            // TODO Send message notifying user
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
