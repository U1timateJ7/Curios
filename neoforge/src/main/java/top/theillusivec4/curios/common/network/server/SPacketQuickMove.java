package top.theillusivec4.curios.common.network.server;

import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketQuickMove(int windowId, int moveIndex) implements CustomPacketPayload {

  public static final Type<SPacketQuickMove> TYPE =
      new Type<>(new ResourceLocation(CuriosConstants.MOD_ID, "quick_move"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketQuickMove> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.INT, SPacketQuickMove::windowId, ByteBufCodecs.INT,
          SPacketQuickMove::moveIndex, SPacketQuickMove::new);

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
