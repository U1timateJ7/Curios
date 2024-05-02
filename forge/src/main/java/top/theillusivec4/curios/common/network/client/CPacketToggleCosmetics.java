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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.network.CustomPayloadEvent;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class CPacketToggleCosmetics {

  private final int windowId;

  public CPacketToggleCosmetics(int windowId) {
    this.windowId = windowId;
  }

  public static void encode(CPacketToggleCosmetics msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windowId);
  }

  public static CPacketToggleCosmetics decode(FriendlyByteBuf buf) {
    return new CPacketToggleCosmetics(buf.readInt());
  }

  public static void handle(CPacketToggleCosmetics msg, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();

      if (sender != null) {
        AbstractContainerMenu container = sender.containerMenu;

        if (container instanceof CuriosContainer curiosContainer &&
            container.containerId == msg.windowId) {
          curiosContainer.toggleCosmetics();
        }
      }
    });
    ctx.setPacketHandled(true);
  }
}
