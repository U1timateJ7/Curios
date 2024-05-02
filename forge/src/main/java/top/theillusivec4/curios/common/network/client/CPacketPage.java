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

public class CPacketPage {

  private final int windowId;
  private final boolean next;

  public CPacketPage(int windowId, boolean next) {
    this.windowId = windowId;
    this.next = next;
  }

  public static void encode(CPacketPage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windowId);
    buf.writeBoolean(msg.next);
  }

  public static CPacketPage decode(FriendlyByteBuf buf) {
    return new CPacketPage(buf.readInt(), buf.readBoolean());
  }

  public static void handle(CPacketPage msg, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();

      if (sender != null) {
        AbstractContainerMenu container = sender.containerMenu;

        if (container instanceof CuriosContainer curiosContainer &&
            container.containerId == msg.windowId) {

          if (msg.next) {
            curiosContainer.nextPage();
          } else {
            curiosContainer.prevPage();
          }
        }
      }
    });
    ctx.setPacketHandled(true);
  }
}
