/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import dynamics.api.IValueProvider;
import dynamics.entity.SyncedEntity;
import dynamics.inventory.DynamicInventory;
import dynamics.inventory.IInventoryProvider;
import dynamics.sync.SyncMap;
import dynamics.sync.SyncableInt;
import dynamics.utils.InventoryUtils;
import dynamics.utils.coord.BlockCoord;

// this is a mess
public class EntityExtruder extends SyncedEntity implements IInventoryProvider {

    private static final String TAG_FACING = "facing";
    private static final String TAG_RUNNING = "running";
    private static final String TAG_FUEL = "fuel";

    private static final int dataWatcherEntries = 6;
    private static final int dataWatcherStart = 32 - dataWatcherEntries;

    // used for placing blocks
    private int slot = 0;

    private SyncableInt fuel;

    protected DynamicInventory inventory = createInventory(37);

    public EntityExtruder(World world) {
        super(world);
        this.setSize(.8f, .8f);
        this.boundingBox.setBounds(.1, .1, .1, .9, 1, .9);
        for (int i = 0; i < dataWatcherEntries; ++i) {
            int j = dataWatcherStart + i;
            if (i == 0 || i == 5) dataWatcher.addObject(j, (byte) 0);
            else if (i == 1) dataWatcher.addObject(j, "");
            else if (i == 2) dataWatcher.addObject(j, (float) 0);
            else dataWatcher.addObject(j, 0);

            dataWatcher.setObjectWatched(j);
        }
    }

    public EntityExtruder(World world, double x, double y, double z, ForgeDirection facing) {
        this (world);
        this.setPosition(x, y, z);
        setFacing(facing);
        setRunning(false);
        setDamageTaken(0);
        setTimeSinceHit(0);
        setSineWave(0);
    }

    @Override
    protected void createSyncedFields() {
        fuel = new SyncableInt();
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getRunning())
            run();
        else
            postRun();

        if (Config.useFuel && !worldObj.isRemote) {
            if (inventory.getStackInSlot(0) != null && TileEntityFurnace.isItemFuel(inventory.getStackInSlot(0)) &&
                    (fuel.get() + TileEntityFurnace.getItemBurnTime(inventory.getStackInSlot(0))) <= Config.maxFuelLevel)
                if (inventory.getStackInSlot(0).getItem().equals(Items.lava_bucket)) {
                    fuel.modify(TileEntityFurnace.getItemBurnTime(inventory.decrStackSize(0, 1)));
                    inventory.setInventorySlotContents(0, new ItemStack(Items.bucket));
                } else {
                    fuel.modify(TileEntityFurnace.getItemBurnTime(inventory.decrStackSize(0, 1)));
                }

            if (fuel.isDirty()) sync();
        }

        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        setPosition(posX, posY, posZ);

        if (getTimeSinceHit() > 0) setTimeSinceHit(getTimeSinceHit() - 1);
        if (getDamageTaken() > 0) setDamageTaken(getDamageTaken() - 1);
    }

    private DynamicInventory createInventory(int size) {
        return new DynamicInventory("extruder", false, size) {
            @Override
            public boolean isUseableByPlayer(EntityPlayer player) {
                return !isDead && player.getDistanceSqToEntity(EntityExtruder.this) < 64;
            }

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                return slot != 0 || TileEntityFurnace.isItemFuel(stack);
            }
        };
    }

    @Override
    public IInventory getInventory() {
        return inventory;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setByte(TAG_FACING, (byte) getFacing().ordinal());
        tag.setBoolean(TAG_RUNNING, getRunning());
        fuel.writeToNBT(tag, TAG_FUEL);
        inventory.writeToNBT(tag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        setFacing(ForgeDirection.getOrientation(tag.getByte(TAG_FACING)));
        setRunning(tag.getBoolean(TAG_RUNNING));
        fuel.readFromNBT(tag, TAG_FUEL);
        inventory.readFromNBT(tag);
    }

    private void run() {
        if (Config.useFuel) {
            if (fuel.get() < Config.fuelPerTick) {
                setRunning(false);
                setMotion(0, 0, 0);
                return;
            }
            fuel.modify(-Config.fuelPerTick);
        }
        setSineWave(getSineWave() + 1);

        if (!worldObj.isRemote) {
            setIsCentered((String.format("%.1f", posX % 1).equals("0.5") || String.format("%.1f", posX % 1).equals("-0.5")) &&
                    (String.format("%.1f", posY % 1).equals("0.0") || String.format("%.1f", posY % 1).equals("1.0")) &&
                    (String.format("%.1f", posZ % 1).equals("0.5") || String.format("%.1f", posZ % 1).equals("-0.5")));
        }

        if (getIsCentered()) {
            if (worldObj.isRemote) {
                worldObj.playAuxSFX(1001, (int) posX, (int) posY, (int) posZ, 0);
            } else {
                BlockCoord front = new BlockCoord(MathHelper.floor_double(posX) + getFacing().offsetX, Math.round(posY) + getFacing().offsetY, MathHelper.floor_double(posZ) + getFacing().offsetZ);
                BlockCoord back = new BlockCoord(MathHelper.floor_double(posX) - getFacing().offsetX, Math.round(posY) - getFacing().offsetY, MathHelper.floor_double(posZ) - getFacing().offsetZ);

                if (Config.useFuel) {
                    if (fuel.get() >= Config.fuelPerBlockMined) {
                        if (breakBlock(front)) fuel.modify(-Config.fuelPerBlockMined);
                    }

                    if (fuel.get() >= Config.fuelPerBlockExtruded) {
                        if (placeBlock(back)) fuel.modify(-Config.fuelPerBlockExtruded);
                    }
                } else {
                    breakBlock(front);
                    placeBlock(back);
                }

                if (worldObj.getBlock(front.x, front.y, front.z).isBlockSolid(worldObj, front.x, front.y, front.z, worldObj.getBlockMetadata(front.x, front.y, front.z))) {
                    setRunning(false);
                    setMotion(0, 0, 0);
                }
            }
        }
    }

    private boolean breakBlock(BlockCoord coord) {
        Block block = worldObj.getBlock(coord.x, coord.y, coord.z);
        int blockMetadata = worldObj.getBlockMetadata(coord.x, coord.y, coord.z);

        if (block.equals(Blocks.obsidian) || block.equals(Blocks.bedrock) || block.equals(Blocks.lava) || block.equals(Blocks.flowing_lava)) {
            setRunning(false);
            setMotion(0, 0, 0);
        } else if (block.isBlockSolid(worldObj, coord.x, coord.y, coord.z, blockMetadata)) {
            if (!Config.destroyBlocks) {
                for (int i = 10; i < 37; ++i) { // runs through slots 10-36 (mined inventory)
                    if (block.getItemDropped(blockMetadata, rand, 0) == null) break; // check for beds, doors...
                    ItemStack blockStack = new ItemStack(block.getItemDropped(blockMetadata, rand, 0), block.quantityDropped(rand), blockMetadata);
                    ItemStack stackInSlot = inventory.getStackInSlot(i);
                    if (blockStack.stackSize == 0) break;
                    if (stackInSlot != null) {
                        if (InventoryUtils.areMergeCandidates(blockStack, stackInSlot)) {
                            if (InventoryUtils.tryMergeStacks(blockStack, stackInSlot)) break;
                        }
                    } else {
                        inventory.setInventorySlotContents(i, blockStack);
                        break;
                    }
                }
            }
            return worldObj.setBlockToAir(coord.x, coord.y, coord.z);
        }

        return false;
    }

    private boolean placeBlock(BlockCoord coord) {
        if (worldObj.getBlock(coord.x, coord.y, coord.z).canPlaceBlockAt(worldObj, coord.x, coord.y, coord.z)) {
            slot++;
            if (slot > 9) slot = 1;
            if (inventory.getStackInSlot(slot) != null) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!(stack.getItem() instanceof ItemBlock)) return false;
                stack = inventory.decrStackSize(slot, 1);
                return worldObj.setBlock(coord.x, coord.y, coord.z, Block.getBlockFromItem(stack.getItem()), stack.getItemDamage(), 2);
            }
        }

        return false;
    }

    private void postRun() {
        if (motionX == 0 && motionY == 0 && motionZ == 0) return;

        run();

        if (getIsCentered()) setMotion(0, 0, 0);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (!worldObj.isRemote && !isDead) {
            player.openGui(Extruder.instance, ExtruderGuiHandler.GuiId.EXTRUDER.ordinal(), player.worldObj, getEntityId(), 0, 0);
            sync();
        }
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (!worldObj.isRemote && !isDead) {
            setTimeSinceHit(10);
            setDamageTaken(this.getDamageTaken() + damage * 10.0F);

            boolean isPlayer = source.getEntity() instanceof EntityPlayer;

            if (this.getDamageTaken() > 20.0F) {
                setDead();
                if (isPlayer) {
                    if (!((EntityPlayer) source.getEntity()).capabilities.isCreativeMode) dropItem(Extruder.Items.extruder, 1);
                    if (source.getEntity().isSprinting()) setMotion(0, 0, 0);
                }

                if (inventory.contents().size() != 0)
                    for (ItemStack stack : inventory.contents())
                        if (stack != null) entityDropItem(stack, 0);
            }

            if (!getRunning())
                setMotion((double) getFacing().offsetX / 10, (double) getFacing().offsetY / 10, (double) getFacing().offsetZ / 10);
            setRunning(!getRunning());
        }

        if (worldObj.isRemote) worldObj.playAuxSFX(1001, (int) posX, (int) posY, (int) posZ, 0);

        return false;
    }

    public void setMotion(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public SyncMap<SyncedEntity> getSyncMap() {
        return syncMap;
    }

    public IValueProvider<Integer> getFuelProvider() {
        return fuel;
    }

    // from here on, there are just the getters and setters for the variables I have on datawatcher

    public ForgeDirection getFacing() {
        return ForgeDirection.getOrientation(dataWatcher.getWatchableObjectByte(dataWatcherStart));
    }

    public void setFacing(ForgeDirection facing) {
        dataWatcher.updateObject(dataWatcherStart, (byte) facing.ordinal());
    }

    public boolean getRunning() {
        return Boolean.valueOf(dataWatcher.getWatchableObjectString(dataWatcherStart + 1));
    }

    public void setRunning(boolean running) {
        dataWatcher.updateObject(dataWatcherStart + 1, String.valueOf(running));
    }

    public float getDamageTaken() {
        return dataWatcher.getWatchableObjectFloat(dataWatcherStart + 2);
    }

    public void setDamageTaken(float damage) {
        dataWatcher.updateObject(dataWatcherStart + 2, damage);
    }

    public int getTimeSinceHit() {
        return dataWatcher.getWatchableObjectInt(dataWatcherStart + 3);
    }

    public void setTimeSinceHit(int time) {
        dataWatcher.updateObject(dataWatcherStart + 3, time);
    }

    public int getSineWave() {
        return dataWatcher.getWatchableObjectInt(dataWatcherStart + 4);
    }

    public void setSineWave(int sineWave) {
        dataWatcher.updateObject(dataWatcherStart + 4, sineWave);
    }

    public boolean getIsCentered() {
        return dataWatcher.getWatchableObjectByte(dataWatcherStart + 5) != 0;
    }

    public void setIsCentered(boolean isOnCenter) {
        dataWatcher.updateObject(dataWatcherStart + 5, isOnCenter ? (byte) 1 : 0);
    }
}