package top.theillusivec4.curios.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.network.CustomPayloadEvent;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketPage {

  private final int windowId;
  private final int page;

  public SPacketPage(int windowId, int page) {
    this.windowId = windowId;
    this.page = page;
  }

  public static void encode(SPacketPage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windowId);
    buf.writeInt(msg.page);
  }

  public static SPacketPage decode(FriendlyByteBuf buf) {
    return new SPacketPage(buf.readInt(), buf.readInt());
  }

  public static void handle(SPacketPage msg, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer clientPlayer = mc.player;
      Screen screen = mc.screen;

      if (clientPlayer != null) {
        AbstractContainerMenu container = clientPlayer.containerMenu;

        if (container instanceof CuriosContainer && container.containerId == msg.windowId) {
          ((CuriosContainer) container).setPage(msg.page);
        }
      }

      if (screen instanceof CuriosScreen) {
        ((CuriosScreen) screen).updateRenderButtons();
      }
    });
    ctx.setPacketHandled(true);
  }
}
