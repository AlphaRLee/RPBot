package com.rlee.discordbots.rpbot.profile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Utility class for printing out a profile
 * @author leeri
 *
 */
public class ProfilePrinter {

	public void printProfileToChannel(CharProfile profile, MessageChannel channel) {
		if (channel == null) {
			return;
		}
		
		String output = "```\n" + printProfile(profile, true) + "```";
		channel.sendMessage(output).queue();
	}
	
	public void writeProfileToFile(CharProfile profile, File file) {
		if (file == null) {
			return;
		}
		
		String output = printProfile(profile, false);
		try (FileWriter fw = new FileWriter(file)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			fw.write(output);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String printProfile(CharProfile profile, boolean formatOutput) {
		if (profile == null) {
			return null;
		}
		
		String output = "Name: " + profile.getName() + "\n"
				+ (profile.getMember() != null ? "Player: " 
				+ profile.getMember().getUser().getName() + "#" + profile.getMember().getUser().getDiscriminator() 
				+ "\n" : "")
				+ "\n";
			
		for (Attribute attribute : profile.getAttributes().values()) {
			output += getAttributeDetail(attribute, formatOutput);
			output += "\n";
		}
		
		return output;
	}
	
	/**
	 * Get a string detailing this attribute.
	 * @param attribute
	 * @param formatOutput Left-justify the name and attempt set the name and ":" character to width 5.
	 * 						Attempt to right-justify the value and set width to 3
	 * @return A string in format: &ltname&gt: &ltvalue&gt[/maxvalue] [+buff (buffduration)]. Returns empty string if attribute == null
	 *
	 * @author R Lee
	 */
	private String getAttributeDetail(Attribute attribute, boolean formatOutput) {
		String output = "";
		
		if (attribute == null) {
			return output;
		}
		
		if (formatOutput) {
			output = String.format("%-5s%3s", attribute.getName() + ":", attribute.getValue());
		} else {
			output = attribute.getName() + ": " + attribute.getValue();
		}
		
		String maxDetail = "";
		if (attribute.hasMaxValue()) {
			maxDetail = "/" + attribute.getMaxValue();
		}
		output += maxDetail;
		
		String buffDetail = "";
		if (attribute.hasBuff()) {
			int buff = attribute.getBuff();
			int dur = attribute.getBuffDuration();
			
			// Prints out buff detail in the format of:
			// "+3 (5 rolls left)
			buffDetail = " " + (buff >= 0 ? "+" : "") + buff
						+ " (" + dur + " roll" + (dur != 1 ? "s" : "") + " left)";
		}
		
		if (!buffDetail.isEmpty()) {
			if (formatOutput && maxDetail.isEmpty()) {
				output += "\t";
			} else {
				output += " ";
			}
			output += buffDetail;
		}
		
		return output;
	}
	
	public void messageAttributeDetail(CharProfile profile, Attribute attribute, MessageChannel channel) {
		if (profile == null || channel == null) {
			return;
		}
		
		String attrDetail = getAttributeDetail(attribute, false);
		if (!attrDetail.isEmpty()) {
			channel.sendMessage(profile.getName() + "'s " + attrDetail).queue();
		}
	}
}
