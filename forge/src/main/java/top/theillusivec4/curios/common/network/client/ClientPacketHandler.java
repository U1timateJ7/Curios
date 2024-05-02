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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.server.SPacketQuickMove;

public class ClientPacketHandler {

  // For some reason this is the only packet that causes class-loading issues on a server
  // todo: Refactor other client packets into this class for best practice
  public static void handlePacket(SPacketQuickMove msg) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    if (player != null && player.containerMenu instanceof CuriosContainer container) {
      container.quickMoveStack(player, msg.moveIndex);
    }
  }
}
