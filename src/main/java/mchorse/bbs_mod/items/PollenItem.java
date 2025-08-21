package mchorse.bbs_mod.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * Pollen item that provides bee-like abilities when consumed
 */
public class PollenItem extends Item
{
    public PollenItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user)
    {
        super.finishUsing(stack, world, user);

        if (!world.isClient && user instanceof ServerPlayerEntity player)
        {
            // Apply bee-like effects
            applyBeeEffects(player);
            
            // Trigger advancement criteria
            Criteria.CONSUME_ITEM.trigger(player, stack);
            
            // Play consumption sound
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                          SoundEvents.ENTITY_BEE_POLLINATE, SoundCategory.PLAYERS, 0.5F, 1.0F);
            
            // Create particle effects
            createPollenParticles(world, player);
            
            player.sendMessage(Text.literal("§eYou feel energized by the pollen! §6Your movements become lighter."), false);
        }

        return stack;
    }

    private void applyBeeEffects(ServerPlayerEntity player)
    {
        // Give beneficial effects that simulate bee-like abilities
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1)); // 1 minute of Speed II
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 1)); // 1 minute of Jump Boost II
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 600, 0)); // 30 seconds of Slow Falling
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0)); // 1 minute of Night Vision
    }

    private void createPollenParticles(World world, PlayerEntity player)
    {
        // Create a burst of pollen particles around the player
        for (int i = 0; i < 15; i++)
        {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = world.random.nextDouble() * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
            
            world.addParticle(ParticleTypes.COMPOSTER, 
                            player.getX() + offsetX, 
                            player.getY() + 1.0 + offsetY, 
                            player.getZ() + offsetZ, 
                            0, 0.1, 0);
        }
        
        // Add some golden particles for visual flair
        for (int i = 0; i < 10; i++)
        {
            double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetY = world.random.nextDouble() * 1.2;
            double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
            
            world.addParticle(ParticleTypes.FALLING_NECTAR, 
                            player.getX() + offsetX, 
                            player.getY() + 1.0 + offsetY, 
                            player.getZ() + offsetZ, 
                            0, 0.05, 0);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack)
    {
        return 32; // Same as food items
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.EAT;
    }

    @Override
    public boolean hasGlint(ItemStack stack)
    {
        return true; // Show enchantment glint to indicate it's special
    }
}