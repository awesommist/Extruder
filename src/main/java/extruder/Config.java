/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import dynamics.config.properties.ConfigProperty;
import dynamics.config.properties.OnlineModifiable;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class Config {

    @OnlineModifiable
    @ConfigProperty(name = "destroyBlocks", category = "misc", comment = "True if the extruder should destroy the blocks it mines. False otherwise")
    public static boolean destroyBlocks = false;

    public static void register() {
        final List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();

        recipeList.add(new ShapelessOreRecipe(Extruder.Items.extruder, Blocks.piston, Blocks.dispenser));
    }
}