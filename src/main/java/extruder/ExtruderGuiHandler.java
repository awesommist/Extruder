/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import dynamics.Log;

import cpw.mods.fml.common.network.IGuiHandler;

public class ExtruderGuiHandler implements IGuiHandler {

    public enum GuiId {
        EXTRUDER;

        public static final GuiId[] VALUES = GuiId.values();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final GuiId guiId = getGuiId(ID);
        if (guiId == null) return null;

        switch (guiId) {
            case EXTRUDER:
                return createExtruderContainer(player, world, x);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        final GuiId guiId = getGuiId(ID);
        if (guiId == null) return null;

        switch (guiId) {
            case EXTRUDER:
                final ContainerExtruder container = createExtruderContainer(player, world, x);
                return container != null ? new GuiExtruder(container) : null;
            default:
                return null;
        }
    }

    private static ContainerExtruder createExtruderContainer(EntityPlayer player, World world, int entityId) {
        final Entity entity = world.getEntityByID(entityId);
        if (entity instanceof EntityExtruder) return new ContainerExtruder(player.inventory, (EntityExtruder) entity);

        Log.warn("Trying to open extruder container for invalid entity %d:%s", entityId, entity);
        return null;
    }

    private static GuiId getGuiId(int id) {
        try {
            return GuiId.VALUES[id];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.warn("Invalid GUI id: %d", id);
            return null;
        }
    }
}