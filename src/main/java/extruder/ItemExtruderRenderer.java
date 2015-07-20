/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import dynamics.utils.render.RenderUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ItemExtruderRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (Minecraft.getMinecraft().theWorld != null) {
            GL11.glPushMatrix();

            EntityExtruder extruder = new EntityExtruder(Minecraft.getMinecraft().theWorld);

            switch (type) {
                case ENTITY:
                    GL11.glTranslatef(0, 0, 0.25f);
                    GL11.glRotatef(-90, 1, 0, 0);
                    GL11.glScalef(0.5f, 0.5f, 0.5f);
                    break;
                case INVENTORY:
                    GL11.glRotatef(-90, 1, 0, 0);
                    GL11.glTranslatef(-0.5f, 0, -0.5f);
                    break;
                default:
                    GL11.glTranslatef(0.5f, 0, 0.5f);
            }

            Render renderer = RenderManager.instance.getEntityRenderObject(extruder);
            if (renderer.getFontRendererFromRenderManager() != null)
                renderer.doRender(extruder, 0, 0, 0, 0, 0.5F);

            GL11.glPopMatrix();
            RenderUtils.disableLightmap();
        }
    }
}