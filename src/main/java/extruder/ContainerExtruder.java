/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import dynamics.container.ContainerInventoryProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerExtruder extends ContainerInventoryProvider<EntityExtruder> {

    public ContainerExtruder(IInventory playerInventory, EntityExtruder extruder) {
        super (playerInventory, extruder);
        addSlotToContainer(new Slot(inventory, 0, 79, 19));
        addInventoryLine(7, 47, 1, 9);
        addInventoryLine(7, 86, 10, 9);
        addInventoryLine(7, 104, 19, 9);
        addInventoryLine(7, 122, 28, 9);
        addPlayerInventorySlots(161);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return inventory.isUseableByPlayer(player) && !getOwner().isDead;
    }
}