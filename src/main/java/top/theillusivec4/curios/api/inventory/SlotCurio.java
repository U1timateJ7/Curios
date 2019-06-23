/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.api.inventory;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SlotCurio extends SlotItemHandler {

    private static AtlasSpriteHolder sprites;
    private static final ResourceLocation GENERIC_SLOT = new ResourceLocation(Curios.MODID, "textures/item/empty_generic_slot.png");
    private final String identifier;
    private final PlayerEntity player;

    public SlotCurio(PlayerEntity player, CurioStackHandler handler, int index, String identifier, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.identifier = identifier;
        this.player = player;
        this.backgroundLocation = new ResourceLocation(Curios.MODID, "textures/item/empty_generic_slot.png");

        if (this.player.world.isRemote && sprites == null) {
            sprites = new AtlasSpriteHolder();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public String getSlotName() {
        String key = "curios.identifier." + identifier;
        if (!I18n.hasKey(key)) {
            return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
        }
        return I18n.format(key);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return hasValidTag(CuriosAPI.getCurioTags(stack.getItem())) && CuriosAPI.getCurio(stack)
                .map(curio -> curio.canEquip(identifier, player)).orElse(true)
                && super.isItemValid(stack);
    }

    protected boolean hasValidTag(Set<String> tags) {
        return tags.contains(identifier);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        ItemStack stack = this.getStack();
        return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
                && CuriosAPI.getCurio(stack).map(curio -> curio.canUnequip(identifier, playerIn)).orElse(true)
                && super.canTakeStack(playerIn);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.texture.TextureAtlasSprite getBackgroundSprite() {
        return sprites != null ? sprites.getSpriteForString(this.identifier) : null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public String getSlotTexture() {
        return CuriosAPI.getIcons().getOrDefault(identifier, GENERIC_SLOT).toString();
    }

    final class AtlasSpriteHolder {

        private final Map<String, TextureAtlasSprite> spriteMap = new HashMap<>();

        TextureAtlasSprite getSpriteForString(String id) {
            return spriteMap.computeIfAbsent(id, key -> {
                ResourceLocation rl = CuriosAPI.getIcons().getOrDefault(id, GENERIC_SLOT);
                return new TextureAtlasSprite(rl, 16, 16) {
                    {
                        func_217789_a(16, 16, 0, 0);
                    }
                };
            });
        }
    }
}
