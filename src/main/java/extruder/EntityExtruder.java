/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dynamics.Log;
import dynamics.inventory.DynamicInventory;
import dynamics.inventory.IInventoryProvider;
import dynamics.utils.coord.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EntityExtruder extends Entity implements IInventoryProvider {

    private static final String TAG_FACING = "facing";
    private static final String TAG_RUNNING = "running";

    private static final int dataWatcherEntries = 5;
    private static final int dataWatcherStart = 32 - dataWatcherEntries;

    protected DynamicInventory inventory = createInventory(37);

    public EntityExtruder(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
        for (int i = 0; i < dataWatcherEntries; ++i) {
            int j = dataWatcherStart + i;
            if (i == 0) dataWatcher.addObject(j, (byte) 0);
            else if (i == 1) dataWatcher.addObject(j, "");
            else if (i == 2) dataWatcher.addObject(j, (float) 0);
            else dataWatcher.addObject(j, (int) 0);

            dataWatcher.setObjectWatched(j);
        }
    }

    public EntityExtruder(World world, float x, float y, float z, ForgeDirection facing) {
        this (world);
        this.setPosition(x, y, z);
        setFacing(facing);
        setRunning(false);
        setDamageTaken(0);
        setTimeSinceHit(0);
        setSineWave(0);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();

        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        setPosition(posX, posY, posZ);
        setMotion(motionX, motionY, motionZ);
        if (getTimeSinceHit() > 0) setTimeSinceHit(getTimeSinceHit() - 1);
        if (getDamageTaken() > 0) setDamageTaken(getDamageTaken() - 1);

        if (getRunning())
            run();
        else
            postRun();
    }

    private DynamicInventory createInventory(int size) {
        return new DynamicInventory("extruder", false, size) {
            @Override
            public boolean isUseableByPlayer(EntityPlayer player) {
                return !isDead && player.getDistanceSqToEntity(EntityExtruder.this) < 64;
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
        inventory.writeToNBT(tag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        setFacing(ForgeDirection.getOrientation(tag.getByte(TAG_FACING)));
        setRunning(tag.getBoolean(TAG_RUNNING));
        inventory.readFromNBT(tag);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (worldObj.isRemote) return false;

        if (!isDead)
            player.openGui(Extruder.instance, ExtruderGuiHandler.GuiId.EXTRUDER.ordinal(), player.worldObj, getEntityId(), 0, 0);
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (!worldObj.isRemote && !isDead) {
            setTimeSinceHit(10);
            setDamageTaken(this.getDamageTaken() + damage * 10.0F);

            boolean isCreative = source.getEntity() instanceof EntityPlayer && ((EntityPlayer) source.getEntity()).capabilities.isCreativeMode;

            if (this.getDamageTaken() > 20.0F) {
                setDead();
                if (!isCreative) dropItem(Extruder.Items.extruder, 1);

                if (inventory.contents().size() != 0)
                    for (ItemStack stack : inventory.contents())
                        if (stack != null) entityDropItem(stack, 0);
            }

            setRunning(!getRunning());
        }

        if (worldObj.isRemote && !isDead) worldObj.playAuxSFX(1001, (int) posX, (int) posY, (int) posZ, 0);

        return true;
    }

    private void run() {
        setSineWave(getSineWave() + 1);
        setMotion((double) getFacing().offsetX / 10, (double) getFacing().offsetY / 10, (double) getFacing().offsetZ / 10);

        BlockCoord front = new BlockCoord(Math.floor(posX) + getFacing().offsetX, Math.floor(posY) + getFacing().offsetY, Math.floor(posZ) + getFacing().offsetZ);
        BlockCoord back = new BlockCoord(Math.floor(posX) - getFacing().offsetX, Math.floor(posY) - getFacing().offsetY, Math.floor(posZ) - getFacing().offsetZ);
        if (isOnCenter()) {
            if (worldObj.isRemote) worldObj.playAuxSFX(1001, (int) posX, (int) posY, (int) posZ, 0);

            if (worldObj.getBlock(front.x, front.y, front.z) instanceof BlockObsidian) {
                setRunning(false);
            } else if (!worldObj.isAirBlock(front.x, front.y, front.z)) {
                setMotion(0, 0, 0);
                worldObj.setBlockToAir(front.x, front.y, front.z);
            }

            if (worldObj.isAirBlock(back.x, back.y, back.z)) {
                Block block = null;
                if (inventory.contents().size() != 0)
                    for (ItemStack stack : inventory.contents())
                        if (stack != null) {
                            stack.stackSize--;
                            block = Block.getBlockFromItem(stack.getItem());
                            if (block != null) worldObj.setBlock(back.x, back.y, back.z, block, stack.getItemDamage(), 3);
                            break;
                        }
            }
        }
    }

    private void postRun() {
        if (motionX == 0 && motionY == 0 && motionZ == 0) return;

        if (isOnCenter()) setMotion(0, 0, 0);
    }

    // there might be a better way to do this
    private boolean isOnCenter() {
        BigDecimal x = new BigDecimal(posX).setScale(1, RoundingMode.HALF_UP);
        BigDecimal y  = new BigDecimal(posY).setScale(1, RoundingMode.HALF_UP);
        BigDecimal z = new BigDecimal(posZ).setScale(1, RoundingMode.HALF_UP);
        float modX = Math.abs(x.floatValue() % 1);
        float modY = Math.abs(y.floatValue() % 1);
        float modZ = Math.abs(z.floatValue() % 1);
        return modX == .5 && modY == 0 && modZ == .5;
    }

    public void setMotion(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;
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
}