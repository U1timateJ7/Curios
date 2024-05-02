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
