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
