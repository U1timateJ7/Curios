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

package top.theillusivec4.curios.common.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import top.theillusivec4.curios.common.network.client.ClientPacketHandler;

public class SPacketQuickMove {

  public final int windowId;
  public final int moveIndex;

  public SPacketQuickMove(int windowId, int moveIndex) {
    this.windowId = windowId;
    this.moveIndex = moveIndex;
  }

  public static void encode(SPacketQuickMove msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windowId);
    buf.writeInt(msg.moveIndex);
  }

  public static SPacketQuickMove decode(FriendlyByteBuf buf) {
    return new SPacketQuickMove(buf.readInt(), buf.readInt());
  }

  public static void handle(SPacketQuickMove msg, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> ClientPacketHandler.handlePacket(msg)));
    ctx.setPacketHandled(true);
  }
}
