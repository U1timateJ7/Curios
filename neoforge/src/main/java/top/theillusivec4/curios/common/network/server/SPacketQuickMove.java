package top.theillusivec4.curios.common.network.server;

import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketQuickMove(int windowId, int moveIndex) implements CustomPacketPayload {

  public static final ResourceLocation
      ID = new ResourceLocation(CuriosConstants.MOD_ID, "quick_move");

  public SPacketQuickMove(final FriendlyByteBuf buf) {
    this(buf.readInt(), buf.readInt());
  }

  @Override
  public void write(@Nonnull FriendlyByteBuf buf) {
    buf.writeInt(this.windowId());
    buf.writeInt(this.moveIndex());
  }

  @Nonnull
  @Override
  public ResourceLocation id() {
    return ID;
  }
}
