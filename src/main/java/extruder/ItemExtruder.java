/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import dynamics.item.DynamicItem;

public class ItemExtruder extends DynamicItem {

    public ItemExtruder() {
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, true);

        if (mop == null) return stack;

        ForgeDirection facing = determineFacing(player);
        EntityExtruder extruder;
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (world.isRemote) {
                player.swingItem();
                return stack;
            }

            switch (mop.sideHit) {
                case 0:
                    extruder = new EntityExtruder(world, mop.blockX + 0.5F, mop.blockY - 1.0F, mop.blockZ + 0.5F, facing);
                    break;
                case 1:
                    extruder = new EntityExtruder(world, mop.blockX + 0.5F, mop.blockY + 1.0F, mop.blockZ + 0.5F, facing);
                    break;
                case 2:
                    extruder = new EntityExtruder(world, mop.blockX + 0.5F, mop.blockY, mop.blockZ - 0.5F, facing);
                    break;
                case 3:
                    extruder = new EntityExtruder(world, mop.blockX + 0.5F, mop.blockY, mop.blockZ + 1.5F, facing);
                    break;
                case 4:
                    extruder = new EntityExtruder(world, mop.blockX - 0.5F, mop.blockY, mop.blockZ + 0.5F, facing);
                    break;
                case 5:
                default:
                    extruder = new EntityExtruder(world, mop.blockX + 1.5F, mop.blockY, mop.blockZ + 0.5F, facing);
                    break;
            }

            world.spawnEntityInWorld(extruder);
            stack.stackSize--;
        }
        return stack;
    }

    private static ForgeDirection determineFacing(EntityPlayer player) {
        if (Math.abs(player.rotationPitch) > 45.0F)
            return player.rotationPitch > 0 ? ForgeDirection.DOWN : ForgeDirection.UP;

        if (player.rotationYaw >= 45 && player.rotationYaw < 135) return ForgeDirection.WEST;
        else if (player.rotationYaw >= 135 && player.rotationYaw < 225) return ForgeDirection.NORTH;
        else if (player.rotationYaw >= 225 && player.rotationYaw < 315) return ForgeDirection.EAST;
        else if (player.rotationYaw >= 315 || player.rotationYaw < 45) return ForgeDirection.SOUTH;
        else return ForgeDirection.UP; // for testing purposes
    }
}