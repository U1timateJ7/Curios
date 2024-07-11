/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.common.network.client;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;
import top.theillusivec4.curios.common.network.server.SPacketBreak;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;
import top.theillusivec4.curios.common.network.server.SPacketPage;
import top.theillusivec4.curios.common.network.server.SPacketQuickMove;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncData;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class CuriosClientPayloadHandler {

  private static final CuriosClientPayloadHandler INSTANCE = new CuriosClientPayloadHandler();

  public static CuriosClientPayloadHandler getInstance() {
    return INSTANCE;
  }

  private static void handle(final IPayloadContext ctx, Runnable handler) {
    ctx.enqueueWork(handler)
        .exceptionally(e -> {
          ctx.disconnect(Component.translatable("curios.networking.failed", e.getMessage()));
          return null;
        });
  }

  public void handle(final SPacketSetIcons data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketQuickMove data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketPage data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketBreak data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncRender data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncModifiers data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncData data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncCurios data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketGrabbedItem data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncStack data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }
}
