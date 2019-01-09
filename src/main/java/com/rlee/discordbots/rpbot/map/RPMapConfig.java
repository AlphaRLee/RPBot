package com.rlee.discordbots.rpbot.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A general class for holding configuration for RPMaps, based per RPGame
 */
public class RPMapConfig {
	private int rowCount, colCount;
	private int rowHeight, colWidth;
	private boolean showBorders;

	/**
	 * Create a new RPMapConfig with the default settings
	 */
	public RPMapConfig() {
		rowCount = 8;
		colCount = 8;
		rowHeight = 1;
		colWidth = 3;
		showBorders = true;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getColCount() {
		return colCount;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public int getColWidth() {
		return colWidth;
	}

	public void setColWidth(int colWidth) {
		this.colWidth = colWidth;
	}

	public boolean isShowBorders() {
		return showBorders;
	}

	public void setShowBorders(boolean showBorders) {
		this.showBorders = showBorders;
	}

	public void readMapConfigFromFile(String configFilePath) throws FileNotFoundException {
		File configFile = new File(configFilePath);
		ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

		if (!configFile.exists()) {
			throw new FileNotFoundException("Config file not found!"); // TODO ensure that a config file always exists
		}

		try {
			ObjectNode mapNode = (ObjectNode) yamlMapper.readTree(configFile).get("map");

			rowCount = yamlMapper.treeToValue(mapNode.get("rowCount"), Integer.class);
			colCount = yamlMapper.treeToValue(mapNode.get("colCount"), Integer.class);
			rowHeight = yamlMapper.treeToValue(mapNode.get("rowHeight"), Integer.class);
			colWidth = yamlMapper.treeToValue(mapNode.get("colWidth"), Integer.class);
			showBorders = yamlMapper.treeToValue(mapNode.get("showBorders"), Boolean.class);

		} catch (JsonProcessingException e) {
			// TODO Send message notifying user
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
