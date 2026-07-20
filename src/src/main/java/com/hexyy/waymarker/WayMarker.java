package com.hexyy.waymarker;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WayMarker implements ModInitializer {
	public static final String MOD_ID = "waymarker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final AttachmentType<PlayerState> PLAYER_STATE = AttachmentRegistry.<PlayerState>builder()
			.persistent(PlayerState.CODEC)
			.initializer(PlayerState::new)
			.buildAndRegister(Identifier.of(MOD_ID, "player_state"));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing WayMarker mod!");

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("loc")
					// /loc add <name>
					.then(CommandManager.literal("add")
							.then(CommandManager.argument("name", StringArgumentType.word())
									.executes(ctx -> {
										try {
											ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
											String name = StringArgumentType.getString(ctx, "name");
											PlayerState state = player.getAttachedOrCreate(PLAYER_STATE);
											Location loc = new Location(name, player.getX(), player.getY(), player.getZ(), ctx.getSource().getWorld().getRegistryKey().getValue());
											if (state.addLocation(loc)) {
												player.setAttached(PLAYER_STATE, state);
												player.sendMessage(
													Text.literal("Location Saved!").formatted(Formatting.GREEN)
														.append(Text.literal("\nName: ").formatted(Formatting.GOLD))
														.append(Text.literal(name).formatted(Formatting.WHITE))
														.append(Text.literal("\nX: ").formatted(Formatting.GOLD))
														.append(Text.literal(String.valueOf((int) loc.getX())).formatted(Formatting.WHITE))
														.append(Text.literal("\nY: ").formatted(Formatting.GOLD))
														.append(Text.literal(String.valueOf((int) loc.getY())).formatted(Formatting.WHITE))
														.append(Text.literal("\nZ: ").formatted(Formatting.GOLD))
														.append(Text.literal(String.valueOf((int) loc.getZ())).formatted(Formatting.WHITE))
														.append(Text.literal("\nDimension: ").formatted(Formatting.GOLD))
														.append(Text.literal(loc.getDimensionName()).formatted(Formatting.AQUA)),
													false
												);
											} else {
												player.sendMessage(
													Text.literal("A location with that name already exists!").formatted(Formatting.RED),
													false
												);
											}
											return Command.SINGLE_SUCCESS;
										} catch (Exception e) {
											LOGGER.error("Error executing /loc add command", e);
											ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
											return 0;
										}
									})
							)
					)
					// /loc list
					.then(CommandManager.literal("list")
							.executes(ctx -> {
								try {
									ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
									PlayerState state = player.getAttachedOrCreate(PLAYER_STATE);
									var locations = state.getLocations();
									if (locations.isEmpty()) {
										player.sendMessage(
											Text.literal("You have no saved locations yet!").formatted(Formatting.GRAY),
											false
										);
									} else {
										player.sendMessage(
											Text.literal("---------- Saved Locations ----------").formatted(Formatting.GOLD),
											false
										);
										for (int i = 0; i < locations.size(); i++) {
											Location loc = locations.get(i);
											player.sendMessage(
												Text.literal((i + 1) + ". ").formatted(Formatting.YELLOW)
													.append(Text.literal(loc.getName()).formatted(Formatting.WHITE))
													.append(Text.literal(" (").formatted(Formatting.GRAY))
													.append(Text.literal(loc.getDimensionName()).formatted(Formatting.AQUA))
													.append(Text.literal(")").formatted(Formatting.GRAY)),
												false
											);
										}
									}
									return Command.SINGLE_SUCCESS;
								} catch (Exception e) {
									LOGGER.error("Error executing /loc list command", e);
									ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
									return 0;
								}
							})
					)
					// /loc remove <name>
					.then(CommandManager.literal("remove")
							.then(CommandManager.argument("name", StringArgumentType.word())
									.executes(ctx -> {
										try {
											ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
											String name = StringArgumentType.getString(ctx, "name");
											PlayerState state = player.getAttachedOrCreate(PLAYER_STATE);
											if (state.removeLocation(name)) {
												player.setAttached(PLAYER_STATE, state);
												player.sendMessage(
													Text.literal("Location Removed!").formatted(Formatting.GREEN)
														.append(Text.literal("\nName: ").formatted(Formatting.GOLD))
														.append(Text.literal(name).formatted(Formatting.WHITE)),
													false
												);
											} else {
												player.sendMessage(
													Text.literal("No location found with that name!").formatted(Formatting.RED),
													false
												);
											}
											return Command.SINGLE_SUCCESS;
										} catch (Exception e) {
											LOGGER.error("Error executing /loc remove command", e);
											ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
											return 0;
										}
									})
							)
					)
					// /loc rename <old> <new>
					.then(CommandManager.literal("rename")
							.then(CommandManager.argument("oldName", StringArgumentType.word())
									.then(CommandManager.argument("newName", StringArgumentType.word())
											.executes(ctx -> {
												try {
													ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
													String oldName = StringArgumentType.getString(ctx, "oldName");
													String newName = StringArgumentType.getString(ctx, "newName");
													PlayerState state = player.getAttachedOrCreate(PLAYER_STATE);
													if (state.renameLocation(oldName, newName)) {
														player.setAttached(PLAYER_STATE, state);
														player.sendMessage(
															Text.literal("Location Renamed!").formatted(Formatting.GREEN)
																.append(Text.literal("\nOld Name: ").formatted(Formatting.GOLD))
																.append(Text.literal(oldName).formatted(Formatting.WHITE))
																.append(Text.literal("\nNew Name: ").formatted(Formatting.GOLD))
																.append(Text.literal(newName).formatted(Formatting.WHITE)),
															false
														);
													} else {
														player.sendMessage(
															Text.literal("Old location not found or new name already exists!").formatted(Formatting.RED),
															false
														);
													}
													return Command.SINGLE_SUCCESS;
												} catch (Exception e) {
													LOGGER.error("Error executing /loc rename command", e);
													ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
													return 0;
												}
											})
									)
							)
					)
					// /loc share <player> <location>
					.then(CommandManager.literal("share")
							.then(CommandManager.argument("player", StringArgumentType.word())
									.then(CommandManager.argument("location", StringArgumentType.word())
											.executes(ctx -> {
												try {
													ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
													String targetName = StringArgumentType.getString(ctx, "player");
													String locName = StringArgumentType.getString(ctx, "location");
													PlayerState senderState = sender.getAttachedOrCreate(PLAYER_STATE);
													Location loc = senderState.getLocation(locName);
													if (loc == null) {
														sender.sendMessage(
															Text.literal("No location found with that name!").formatted(Formatting.RED),
															false
														);
														return Command.SINGLE_SUCCESS;
													}
													MinecraftServer server = ctx.getSource().getServer();
													ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
													if (target == null) {
														sender.sendMessage(
															Text.literal("Player not found!").formatted(Formatting.RED),
															false
														);
														return Command.SINGLE_SUCCESS;
													}
													// Send message to target
													target.sendMessage(
														Text.literal(sender.getName().getString()).formatted(Formatting.GOLD)
															.append(Text.literal(" shared a location with you!").formatted(Formatting.GREEN))
															.append(Text.literal("\nName: ").formatted(Formatting.GOLD))
															.append(Text.literal(loc.getName()).formatted(Formatting.WHITE))
															.append(Text.literal("\nX: ").formatted(Formatting.GOLD))
															.append(Text.literal(String.valueOf((int) loc.getX())).formatted(Formatting.WHITE))
															.append(Text.literal("\nY: ").formatted(Formatting.GOLD))
															.append(Text.literal(String.valueOf((int) loc.getY())).formatted(Formatting.WHITE))
															.append(Text.literal("\nZ: ").formatted(Formatting.GOLD))
															.append(Text.literal(String.valueOf((int) loc.getZ())).formatted(Formatting.WHITE))
															.append(Text.literal("\nDimension: ").formatted(Formatting.GOLD))
															.append(Text.literal(loc.getDimensionName()).formatted(Formatting.AQUA)),
														false
													);
													// Send message to sender
													sender.sendMessage(
														Text.literal("Location shared successfully with ").formatted(Formatting.GREEN)
															.append(Text.literal(targetName).formatted(Formatting.GOLD))
															.append(Text.literal("!").formatted(Formatting.GREEN)),
														false
													);
													return Command.SINGLE_SUCCESS;
												} catch (Exception e) {
													LOGGER.error("Error executing /loc share command", e);
													ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
													return 0;
												}
											})
									)
							)
					)
					// /loc <name>
					.then(CommandManager.argument("name", StringArgumentType.word())
							.executes(ctx -> {
								try {
									ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
									String name = StringArgumentType.getString(ctx, "name");
									PlayerState state = player.getAttachedOrCreate(PLAYER_STATE);
									Location loc = state.getLocation(name);
									if (loc != null) {
										player.sendMessage(
											Text.literal("---------- Location Details ----------").formatted(Formatting.GOLD)
												.append(Text.literal("\nName: ").formatted(Formatting.GOLD))
												.append(Text.literal(name).formatted(Formatting.WHITE))
												.append(Text.literal("\nX: ").formatted(Formatting.GOLD))
												.append(Text.literal(String.valueOf((int) loc.getX())).formatted(Formatting.WHITE))
												.append(Text.literal("\nY: ").formatted(Formatting.GOLD))
												.append(Text.literal(String.valueOf((int) loc.getY())).formatted(Formatting.WHITE))
												.append(Text.literal("\nZ: ").formatted(Formatting.GOLD))
												.append(Text.literal(String.valueOf((int) loc.getZ())).formatted(Formatting.WHITE))
												.append(Text.literal("\nDimension: ").formatted(Formatting.GOLD))
												.append(Text.literal(loc.getDimensionName()).formatted(Formatting.AQUA)),
											false
										);
									} else {
										player.sendMessage(
											Text.literal("No location found with that name!").formatted(Formatting.RED),
											false
										);
									}
									return Command.SINGLE_SUCCESS;
								} catch (Exception e) {
									LOGGER.error("Error executing /loc <name> command", e);
									ctx.getSource().sendError(Text.literal("An error occurred: " + e.getMessage()));
									return 0;
								}
							})
					)
			);
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
