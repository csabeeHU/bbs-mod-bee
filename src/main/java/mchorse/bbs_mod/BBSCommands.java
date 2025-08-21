package mchorse.bbs_mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mchorse.bbs_mod.data.DataToString;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.mixin.LevelPropertiesAccessor;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.network.ServerNetwork;
import mchorse.bbs_mod.settings.Settings;
import mchorse.bbs_mod.settings.values.ValueGroup;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.LevelInfo;

import java.util.Collection;
import java.util.function.Predicate;

public class BBSCommands
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
    {
        Predicate<ServerCommandSource> hasPermissions = (source) -> source.hasPermissionLevel(2);
        LiteralArgumentBuilder<ServerCommandSource> bbs = CommandManager.literal("bbs").requires((source) -> true);

        registerMorphCommand(bbs, environment, hasPermissions);
        registerMorphEntityCommand(bbs, environment, hasPermissions);
        registerFilmsCommand(bbs, environment, hasPermissions);
        registerDCCommand(bbs, environment, hasPermissions);
        registerOnHeadCommand(bbs, environment, hasPermissions);
        registerConfigCommand(bbs, environment, hasPermissions);
        registerServerCommand(bbs, environment, hasPermissions);
        registerCheatsCommand(bbs, environment);
        registerBoomCommand(bbs, environment, hasPermissions);
        registerStructureSaveCommand(bbs, environment, hasPermissions);
        registerBeeCommands(bbs, environment, hasPermissions);

        dispatcher.register(bbs);
    }

    private static void registerStructureSaveCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> structures = CommandManager.literal("structures");
        LiteralArgumentBuilder<ServerCommandSource> save = CommandManager.literal("save");
        RequiredArgumentBuilder<ServerCommandSource, String> name = CommandManager.argument("name", StringArgumentType.word());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> from = CommandManager.argument("from", BlockPosArgumentType.blockPos());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> to = CommandManager.argument("to", BlockPosArgumentType.blockPos());

        bbs.then(structures
            .then(save.then(name.then(from.then(to
                .executes(BBSCommands::saveStructure))))
        ));
    }

    private static void registerMorphCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> morph = CommandManager.literal("morph");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.players());
        RequiredArgumentBuilder<ServerCommandSource, String> form = CommandManager.argument("form", StringArgumentType.greedyString());

        morph.then(target
            .executes(BBSCommands::morphCommandDemorph)
            .then(form.executes(BBSCommands::morphCommandMorph)));

        bbs.then(morph.requires(hasPermissions));
    }

    private static void registerMorphEntityCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> morph = CommandManager.literal("morph_entity");

        morph.executes((source) ->
        {
            Entity entity = source.getSource().getEntity();

            if (entity instanceof ServerPlayerEntity player)
            {
                Form form = Morph.getMobForm(player);

                if (form != null)
                {
                    ServerNetwork.sendMorphToTracked(player, form);
                    Morph.getMorph(entity).setForm(FormUtils.copy(form));
                }
            }

            return 1;
        });

        bbs.then(morph.requires(hasPermissions));
    }

    private static void registerFilmsCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> scene = CommandManager.literal("films");
        LiteralArgumentBuilder<ServerCommandSource> play = CommandManager.literal("play");
        LiteralArgumentBuilder<ServerCommandSource> stop = CommandManager.literal("stop");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.players());
        RequiredArgumentBuilder<ServerCommandSource, String> playFilm = CommandManager.argument("film", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, String> stopFilm = CommandManager.argument("film", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> camera = CommandManager.argument("camera", BoolArgumentType.bool());

        playFilm.suggests((ctx, builder) ->
        {
            for (String key : BBSMod.getFilms().getKeys())
            {
                builder.suggest(key);
            }

            return builder.buildFuture();
        });

        stopFilm.suggests((ctx, builder) ->
        {
            for (String key : BBSMod.getFilms().getKeys())
            {
                builder.suggest(key);
            }

            return builder.buildFuture();
        });

        scene.then(
            target.then(
                play.then(
                    playFilm.executes((source) -> sceneCommandPlay(source, true))
                        .then(
                            camera.executes((source) -> sceneCommandPlay(source, BoolArgumentType.getBool(source, "camera")))
                        )
                )
            )
            .then(
                stop.then(
                    stopFilm.executes(BBSCommands::sceneCommandStop)
                )
            )
        );

        bbs.then(scene.requires(hasPermissions));
    }

    private static void registerDCCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> dc = CommandManager.literal("dc");
        LiteralArgumentBuilder<ServerCommandSource> shutdown = CommandManager.literal("shutdown");
        LiteralArgumentBuilder<ServerCommandSource> start = CommandManager.literal("start");
        LiteralArgumentBuilder<ServerCommandSource> stop = CommandManager.literal("stop");

        bbs.then(
            dc.requires(hasPermissions).then(start.executes(BBSCommands::DCCommandStart))
                .then(stop.executes(BBSCommands::DCCommandStop))
                .then(shutdown.executes(BBSCommands::DCCommandShutdown))
        );
    }

    private static void registerOnHeadCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> onHead = CommandManager.literal("on_head");

        bbs.then(onHead.requires(hasPermissions).executes(BBSCommands::onHead));
    }

    private static void registerConfigCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> config = CommandManager.literal("config");

        config.requires((ctx) -> ctx.hasPermissionLevel(4)).then(
            CommandManager.literal("set").then(
                CommandManager.argument("option", StringArgumentType.word())
                    .suggests((ctx, builder) ->
                    {
                        Settings settings = BBSMod.getSettings().modules.get("bbs");

                        if (settings != null)
                        {
                            for (ValueGroup value : settings.categories.values())
                            {
                                for (BaseValue baseValue : value.getAll())
                                {
                                    builder.suggest(value.getId() + "." + baseValue.getId());
                                }
                            }
                        }

                        return builder.buildFuture();
                    })
                    .then(
                        CommandManager.argument("value", StringArgumentType.greedyString()).executes((ctx) ->
                        {
                            Settings settings = BBSMod.getSettings().modules.get("bbs");

                            if (settings != null)
                            {
                                String option = StringArgumentType.getString(ctx, "option");
                                String value = StringArgumentType.getString(ctx, "value");
                                BaseType valueType = DataToString.fromString(value);
                                String[] split = option.split("\\.");

                                if (valueType != null && split.length >= 2)
                                {
                                    BaseValue baseValue = settings.get(split[0], split[1]);

                                    if (baseValue != null)
                                    {
                                        baseValue.fromData(valueType);
                                        settings.saveLater();
                                    }
                                }
                            }

                            return 1;
                        })
                    )
            )
        );

        bbs.then(config.requires(hasPermissions));
    }

    private static void registerServerCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> server = CommandManager.literal("server");

        server.then(
            CommandManager.literal("assets").executes((ctx) ->
            {
                for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList())
                {
                    ServerNetwork.sendHandshake(ctx.getSource().getServer(), player);
                }

                return 1;
            })
        ).then(
            CommandManager.literal("asset_manager").then(CommandManager.argument("manager", EntityArgumentType.player()).executes((ctx) ->
            {
                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "manager");

                BBSSettings.serverAssetManager.set(player.getUuidAsString());

                return 1;
            }))
        );

        bbs.then(server.requires(hasPermissions));
    }

    private static void registerCheatsCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        if (environment.dedicated)
        {
            return;
        }

        bbs.then(
            CommandManager.literal("cheats").then(
                CommandManager.argument("enabled", BoolArgumentType.bool()).executes((ctx) ->
                {
                    MinecraftServer server = ctx.getSource().getServer();
                    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                    SaveProperties saveProperties = server.getSaveProperties();

                    if (saveProperties instanceof LevelPropertiesAccessor accessor)
                    {
                        LevelInfo levelInfo = saveProperties.getLevelInfo();

                        accessor.bbs$setLevelInfo(new LevelInfo(levelInfo.getLevelName(),
                            levelInfo.getGameMode(),
                            levelInfo.isHardcore(),
                            levelInfo.getDifficulty(),
                            enabled,
                            levelInfo.getGameRules(),
                            levelInfo.getDataConfiguration()
                        ));

                        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList())
                        {
                            server.getCommandManager().sendCommandTree(serverPlayerEntity);
                            ServerNetwork.sendCheatsPermission(serverPlayerEntity, enabled);
                        }
                    }

                    return 1;
                })
            )
        );
    }

    private static void registerBoomCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        bbs.then(
            CommandManager.literal("boom").requires(hasPermissions).then(
                CommandManager.argument("pos", Vec3ArgumentType.vec3()).then(
                    CommandManager.argument("radius", FloatArgumentType.floatArg(1)).then(
                        CommandManager.argument("fire", BoolArgumentType.bool()).executes((ctx) ->
                        {
                            ServerCommandSource source = ctx.getSource();
                            Vec3d pos = Vec3ArgumentType.getVec3(ctx, "pos");
                            float radius = FloatArgumentType.getFloat(ctx, "radius");
                            boolean fire = BoolArgumentType.getBool(ctx, "fire");

                            source.getWorld().createExplosion(null, pos.x, pos.y, pos.z, radius, fire, World.ExplosionSourceType.BLOCK);

                            return 1;
                        })
                    )
                )
            )
        );
    }

    /**
     * /bbs morph McHorseYT - demorph (remove morph) player McHorseYT
     */
    private static int morphCommandDemorph(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        ServerPlayerEntity entity = EntityArgumentType.getPlayer(source, "target");

        ServerNetwork.sendMorphToTracked(entity, null);
        Morph.getMorph(entity).setForm(null);

        return 1;
    }

    /**
     * /bbs morph McHorse {id:"bbs:model",model:"butterfly",texture:"assets:models/butterfly/yellow.png"}
     *
     * Morphs player McHorseYT into a butterfly model with yellow skin
     */
    private static int morphCommandMorph(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        String formData = StringArgumentType.getString(source, "form");

        try
        {
            Form form = FormUtils.fromData(DataToString.mapFromString(formData));

            for (ServerPlayerEntity player : players)
            {
                ServerNetwork.sendMorphToTracked(player, form);
                Morph.getMorph(player).setForm(FormUtils.copy(form));
            }

            return 1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * /bbs film McHorseYT play test - Plays a film (with camera) to McHorseYT
     * /bbs film @a play test false - Plays a film (without camera) to all players
     */
    private static int sceneCommandPlay(CommandContext<ServerCommandSource> source, boolean withCamera) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        String filmId = StringArgumentType.getString(source, "film");

        for (ServerPlayerEntity player : players)
        {
            ServerNetwork.sendPlayFilm(player, filmId, withCamera);
        }

        return 1;
    }

    /**
     * /bbs film McHorseYT stop test - Stops film playback
     */
    private static int sceneCommandStop(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        String filmId = StringArgumentType.getString(source, "film");

        for (ServerPlayerEntity player : players)
        {
            ServerNetwork.sendStopFilm(player, filmId);
        }

        return 1;
    }

    private static int DCCommandShutdown(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().resetDamage(source.getSource().getWorld());

        return 1;
    }

    private static int DCCommandStart(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().trackDamage(source.getSource().getWorld());

        return 1;
    }

    private static int DCCommandStop(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().stopDamage(source.getSource().getWorld());

        return 1;
    }

    private static int onHead(CommandContext<ServerCommandSource> source)
    {
        if (source.getSource().getEntity() instanceof LivingEntity livingEntity)
        {
            ItemStack stack = livingEntity.getEquippedStack(EquipmentSlot.MAINHAND);

            if (!stack.isEmpty())
            {
                livingEntity.equipStack(EquipmentSlot.HEAD, stack.copy());
            }
        }

        return 1;
    }

    private static int saveStructure(CommandContext<ServerCommandSource> source)
    {
        String name = StringArgumentType.getString(source, "name");
        BlockPos from = BlockPosArgumentType.getBlockPos(source, "from");
        BlockPos to = BlockPosArgumentType.getBlockPos(source, "to");

        ServerWorld world = source.getSource().getWorld();
        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
        StructureTemplate structureTemplate;

        try
        {
            structureTemplate = structureTemplateManager.getTemplateOrBlank(new Identifier(name));
        }
        catch (InvalidIdentifierException e)
        {
            return 0;
        }

        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        BlockPos size = max.subtract(min).add(1, 1, 1);

        structureTemplate.saveFromWorld(world, min, size, true, Blocks.STRUCTURE_VOID);

        try
        {
            if (structureTemplateManager.saveTemplate(new Identifier(name)))
            {
                return 1;
            }
        }
        catch (InvalidIdentifierException var7)
        {}

        return 0;
    }

    /**
     * Register bee-related commands for the BBS mod bee extension
     */
    private static void registerBeeCommands(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment, Predicate<ServerCommandSource> hasPermissions)
    {
        LiteralArgumentBuilder<ServerCommandSource> bee = CommandManager.literal("bee");
        
        // /bbs bee buzz [player] - Make player buzz like a bee
        LiteralArgumentBuilder<ServerCommandSource> buzz = CommandManager.literal("buzz");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> buzzTarget = CommandManager.argument("target", EntityArgumentType.players());
        buzz.executes(BBSCommands::buzzSelf)
            .then(buzzTarget.executes(BBSCommands::buzzPlayer));
        
        // /bbs bee swarm [players] - Group players into a bee swarm formation
        LiteralArgumentBuilder<ServerCommandSource> swarm = CommandManager.literal("swarm");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> swarmTargets = CommandManager.argument("targets", EntityArgumentType.players());
        swarm.then(swarmTargets.executes(BBSCommands::createSwarm));
        
        // /bbs bee pollinate [radius] - Create pollen effects around flowers
        LiteralArgumentBuilder<ServerCommandSource> pollinate = CommandManager.literal("pollinate");
        RequiredArgumentBuilder<ServerCommandSource, Float> radius = CommandManager.argument("radius", FloatArgumentType.floatArg(1.0F, 20.0F));
        pollinate.executes(BBSCommands::pollinateNear)
                 .then(radius.executes(BBSCommands::pollinateWithRadius));
        
        // /bbs bee transform [player] [bee_type] - Transform player into specific bee type
        LiteralArgumentBuilder<ServerCommandSource> transform = CommandManager.literal("transform");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> transformTarget = CommandManager.argument("target", EntityArgumentType.players());
        RequiredArgumentBuilder<ServerCommandSource, String> beeType = CommandManager.argument("bee_type", StringArgumentType.word());
        transform.then(transformTarget
                .executes(BBSCommands::transformToBeeDefault)
                .then(beeType.executes(BBSCommands::transformToBeeType)));

        bee.then(buzz.requires(hasPermissions))
           .then(swarm.requires(hasPermissions))
           .then(pollinate.requires(hasPermissions))
           .then(transform.requires(hasPermissions));
        
        bbs.then(bee);
    }

    /**
     * /bbs bee buzz - Make the command sender buzz like a bee
     */
    private static int buzzSelf(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        ServerCommandSource commandSource = source.getSource();
        if (commandSource.getEntity() instanceof ServerPlayerEntity player)
        {
            return createBuzzEffect(player);
        }
        return 0;
    }

    /**
     * /bbs bee buzz [player] - Make target player buzz like a bee
     */
    private static int buzzPlayer(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        int count = 0;
        
        for (ServerPlayerEntity player : players)
        {
            count += createBuzzEffect(player);
        }
        
        return count;
    }

    private static int createBuzzEffect(ServerPlayerEntity player)
    {
        World world = player.getWorld();
        
        // Create buzzing particle effect around player
        for (int i = 0; i < 30; i++)
        {
            double angle = (i * Math.PI * 2) / 30;
            double radius = 1.5;
            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getY() + 1.0 + Math.sin(i * 0.3) * 0.3;
            double z = player.getZ() + Math.sin(angle) * radius;
            
            world.addParticle(net.minecraft.particle.ParticleTypes.FALLING_NECTAR, x, y, z, 0, 0.05, 0);
        }
        
        // Play bee buzzing sound
        world.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_BEE_LOOP, 
                       net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
        
        player.sendMessage(net.minecraft.text.Text.literal("§e*Buzz buzz* §6You're buzzing like a bee!"), false);
        
        return 1;
    }

    /**
     * /bbs bee swarm [players] - Arrange players in a bee swarm formation
     */
    private static int createSwarm(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "targets");
        
        if (players.size() < 2)
        {
            source.getSource().sendMessage(net.minecraft.text.Text.literal("§cNeed at least 2 players to create a swarm!"));
            return 0;
        }
        
        ServerPlayerEntity leader = players.iterator().next();
        Vec3d centerPos = leader.getPos();
        
        int index = 0;
        for (ServerPlayerEntity player : players)
        {
            if (index == 0)
            {
                // Leader stays in place
                player.sendMessage(net.minecraft.text.Text.literal("§eYou are now the swarm leader! §6Buzz buzz!"), false);
            }
            else
            {
                // Arrange other players in a circle around the leader
                double angle = (index * Math.PI * 2) / (players.size() - 1);
                double radius = 3.0;
                double x = centerPos.x + Math.cos(angle) * radius;
                double z = centerPos.z + Math.sin(angle) * radius;
                
                player.teleport(x, centerPos.y, z);
                player.sendMessage(net.minecraft.text.Text.literal("§eYou've joined the bee swarm! §6Follow your leader!"), false);
            }
            
            // Create buzz effect for all players
            createBuzzEffect(player);
            index++;
        }
        
        return players.size();
    }

    /**
     * /bbs bee pollinate - Create pollen effects around nearby flowers (default radius)
     */
    private static int pollinateNear(CommandContext<ServerCommandSource> source)
    {
        return pollinateArea(source, 10.0F);
    }

    /**
     * /bbs bee pollinate [radius] - Create pollen effects around flowers in specified radius
     */
    private static int pollinateWithRadius(CommandContext<ServerCommandSource> source)
    {
        float radius = FloatArgumentType.getFloat(source, "radius");
        return pollinateArea(source, radius);
    }

    private static int pollinateArea(CommandContext<ServerCommandSource> source, float radius)
    {
        ServerCommandSource commandSource = source.getSource();
        World world = commandSource.getWorld();
        Vec3d pos = commandSource.getPosition();
        
        int pollinatedFlowers = 0;
        int radiusInt = (int) radius;
        
        for (int dx = -radiusInt; dx <= radiusInt; dx++)
        {
            for (int dy = -3; dy <= 3; dy++)
            {
                for (int dz = -radiusInt; dz <= radiusInt; dz++)
                {
                    BlockPos blockPos = new BlockPos((int)pos.x + dx, (int)pos.y + dy, (int)pos.z + dz);
                    String blockName = world.getBlockState(blockPos).getBlock().getTranslationKey();
                    
                    if (blockName.contains("flower") || blockName.contains("rose") || 
                        blockName.contains("tulip") || blockName.contains("dandelion") || 
                        blockName.contains("poppy"))
                    {
                        // Create pollen particles above flowers
                        for (int i = 0; i < 5; i++)
                        {
                            world.addParticle(net.minecraft.particle.ParticleTypes.COMPOSTER, 
                                            blockPos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5, 
                                            blockPos.getY() + 1.2 + world.random.nextDouble() * 0.3, 
                                            blockPos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5, 
                                            0, 0.1, 0);
                        }
                        pollinatedFlowers++;
                    }
                }
            }
        }
        
        if (pollinatedFlowers > 0)
        {
            // Play pollination sound
            world.playSound(null, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), 
                           net.minecraft.sound.SoundEvents.ENTITY_BEE_POLLINATE, 
                           net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);
            
            commandSource.sendMessage(net.minecraft.text.Text.literal("§ePollinated " + pollinatedFlowers + " flowers! §6The bees are happy!"));
        }
        else
        {
            commandSource.sendMessage(net.minecraft.text.Text.literal("§cNo flowers found in the area to pollinate."));
        }
        
        return pollinatedFlowers;
    }

    /**
     * /bbs bee transform [player] - Transform player into default honey bee
     */
    private static int transformToBeeDefault(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        return transformPlayerToBee(source, "honey");
    }

    /**
     * /bbs bee transform [player] [bee_type] - Transform player into specific bee type
     */
    private static int transformToBeeType(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        String beeType = StringArgumentType.getString(source, "bee_type");
        return transformPlayerToBee(source, beeType);
    }

    private static int transformPlayerToBee(CommandContext<ServerCommandSource> source, String beeType) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        int count = 0;
        
        for (ServerPlayerEntity player : players)
        {
            try
            {
                mchorse.bbs_mod.forms.forms.BeeForm beeForm = new mchorse.bbs_mod.forms.forms.BeeForm();
                
                // Configure bee based on type
                switch (beeType.toLowerCase())
                {
                    case "honey":
                        beeForm.texture.set(mchorse.bbs_mod.resources.Link.assets("textures/bee/honey.png"));
                        beeForm.beeType.set("honey");
                        beeForm.wingSpeed.set(1.0F);
                        beeForm.size.set(1.0F);
                        beeForm.stripeColor.set(mchorse.bbs_mod.utils.colors.Color.create(255, 255, 0));
                        break;
                    case "bumble":
                        beeForm.texture.set(mchorse.bbs_mod.resources.Link.assets("textures/bee/bumble.png"));
                        beeForm.beeType.set("bumble");
                        beeForm.wingSpeed.set(0.8F);
                        beeForm.size.set(1.2F);
                        beeForm.stripeColor.set(mchorse.bbs_mod.utils.colors.Color.create(255, 200, 0));
                        break;
                    case "carpenter":
                        beeForm.texture.set(mchorse.bbs_mod.resources.Link.assets("textures/bee/carpenter.png"));
                        beeForm.beeType.set("carpenter");
                        beeForm.wingSpeed.set(1.2F);
                        beeForm.size.set(0.9F);
                        beeForm.stripeColor.set(mchorse.bbs_mod.utils.colors.Color.create(139, 69, 19));
                        break;
                    default:
                        // Default to honey bee
                        beeForm.texture.set(mchorse.bbs_mod.resources.Link.assets("textures/bee/honey.png"));
                        beeForm.beeType.set("honey");
                        beeForm.wingSpeed.set(1.0F);
                        beeForm.size.set(1.0F);
                        beeForm.stripeColor.set(mchorse.bbs_mod.utils.colors.Color.create(255, 255, 0));
                        break;
                }
                
                ServerNetwork.sendMorphToTracked(player, beeForm);
                Morph.getMorph(player).setForm(FormUtils.copy(beeForm));
                
                player.sendMessage(net.minecraft.text.Text.literal("§eYou have transformed into a " + beeType + " bee! §6Buzz buzz!"), false);
                createBuzzEffect(player);
                count++;
            }
            catch (Exception e)
            {
                player.sendMessage(net.minecraft.text.Text.literal("§cFailed to transform into bee: " + e.getMessage()), false);
            }
        }
        
        return count;
    }
}