package mchorse.bbs_mod.items;

import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.BeeForm;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.network.ServerNetwork;
import mchorse.bbs_mod.resources.Link;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Honey Wand item that provides bee-related magical abilities
 */
public class HoneyWandItem extends Item
{
    public HoneyWandItem(Settings settings)
    {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
    {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient)
        {
            if (user.isSneaking())
            {
                // Sneaking: Transform user into a bee
                transformToBee((ServerPlayerEntity) user);
            }
            else
            {
                // Normal use: Create honey/pollen effects around user
                createHoneyEffects(world, user);
            }

            // Play bee sound
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BEE_LOOP, 
                          SoundCategory.PLAYERS, 0.5F, 1.0F);
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    private void transformToBee(ServerPlayerEntity player)
    {
        try
        {
            BeeForm beeForm = new BeeForm();
            beeForm.texture.set(Link.assets("textures/bee/honey.png"));
            beeForm.beeType.set("honey");
            beeForm.wingSpeed.set(1.2F);
            beeForm.size.set(0.8F);

            ServerNetwork.sendMorphToTracked(player, beeForm);
            Morph.getMorph(player).setForm(FormUtils.copy(beeForm));

            player.sendMessage(Text.literal("§eYou have transformed into a honey bee! §6Buzz buzz!"), false);
        }
        catch (Exception e)
        {
            player.sendMessage(Text.literal("§cFailed to transform into bee: " + e.getMessage()), false);
        }
    }

    private void createHoneyEffects(World world, PlayerEntity user)
    {
        Vec3d pos = user.getPos();
        
        // Create golden particle spiral around the player
        for (int i = 0; i < 20; i++)
        {
            double angle = (i * Math.PI * 2) / 20;
            double radius = 2.0;
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + 1.0 + Math.sin(i * 0.5) * 0.5;
            double z = pos.z + Math.sin(angle) * radius;
            
            world.addParticle(ParticleTypes.FALLING_NECTAR, x, y, z, 0, -0.1, 0);
        }

        // Create honey effects on nearby flowers
        for (int dx = -5; dx <= 5; dx++)
        {
            for (int dy = -2; dy <= 2; dy++)
            {
                for (int dz = -5; dz <= 5; dz++)
                {
                    BlockPos blockPos = user.getBlockPos().add(dx, dy, dz);
                    if (isFlower(world, blockPos))
                    {
                        // Add pollen particles above flowers
                        world.addParticle(ParticleTypes.COMPOSTER, 
                                        blockPos.getX() + 0.5, 
                                        blockPos.getY() + 1.2, 
                                        blockPos.getZ() + 0.5, 
                                        0, 0.1, 0);
                    }
                }
            }
        }
    }

    private boolean isFlower(World world, BlockPos pos)
    {
        String blockName = world.getBlockState(pos).getBlock().getTranslationKey();
        return blockName.contains("flower") || 
               blockName.contains("rose") || 
               blockName.contains("tulip") ||
               blockName.contains("dandelion") ||
               blockName.contains("poppy");
    }

    @Override
    public boolean hasGlint(ItemStack stack)
    {
        return true; // Always show enchantment glint to indicate it's magical
    }
}