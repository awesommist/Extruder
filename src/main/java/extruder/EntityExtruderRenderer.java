/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityExtruderRenderer extends Render {

    private static final ResourceLocation model = new ResourceLocation("extruder", "models/extruder.obj");
    private static final ResourceLocation texture = new ResourceLocation("extruder", "textures/entities/extruder.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
        EntityExtruder extruder;
        if (entity instanceof EntityExtruder) extruder = (EntityExtruder) entity;
        else return;

        GL11.glPushMatrix();

        bindTexture(texture);
        preRender(extruder, x, y, z, yaw, partialTickTime);
        renderBase(extruder, x, y, z, yaw, partialTickTime);
        renderDrill(extruder, x, y, z, yaw, partialTickTime);

        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return texture;
    }

    // this rotation is a bit messed up
    private void preRender(EntityExtruder extruder, double x, double y, double z, float yaw, float partialTickTime) {
        if (extruder.getDamageTaken() == 0) return;

        ForgeDirection facing = extruder.getFacing();
        GL11.glTranslatef(0, -0.25F, 0.5F);
        float f1 = (float) extruder.getTimeSinceHit() - partialTickTime;
        float f2 = extruder.getDamageTaken() - partialTickTime;
        GL11.glRotatef(MathHelper.sin(f1) * f1 * f2 / 25, facing.offsetX, facing.offsetY, facing.offsetZ);
        GL11.glTranslatef(0, 0.25F, -0.5F);
    }

    private void renderBase(EntityExtruder extruder, double x, double y, double z, float yaw, float partialTickTime) {
        GL11.glTranslated(x, y, z);
        float angle = determineAngle(extruder);
        switch (extruder.getFacing()) {
            case DOWN:
                GL11.glRotatef(angle, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.5F, -0.5F);
                break;
            case UP:
                GL11.glRotatef(angle, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.5F, 0.5F);
                break;
            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
                break;
        }

        AdvancedModelLoader.loadModel(model).renderPart("Base");
    }

    // not smooth at all
    private void renderDrill(EntityExtruder extruder, double x, double y, double z, float yaw, float partialTickTime) {
        GL11.glTranslatef(0.0F, 0.0F, -0.3125F);

        GL11.glTranslatef(0, 0, MathHelper.sin(extruder.getSineWave()) / 8);

        AdvancedModelLoader.loadModel(model).renderPart("Drill");
    }

    private float determineAngle(EntityExtruder extruder) {
        switch (extruder.getFacing()) {
            case DOWN:
                return 90.0F;
            case UP:
                return -90.0F;
            case NORTH:
                return 180.0F;
            case SOUTH:
                return 0.0F;
            case WEST:
                return -90.0F;
            case EAST:
            default:
                return 90.0F;
        }
    }
}