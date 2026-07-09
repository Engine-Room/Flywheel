package dev.engine_room.vanillin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

public class Vanillin {
	public static final String ID = "vanillin";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final Logger CONFIG_LOGGER = LoggerFactory.getLogger(ID + "/config");

	public static Identifier rl(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
