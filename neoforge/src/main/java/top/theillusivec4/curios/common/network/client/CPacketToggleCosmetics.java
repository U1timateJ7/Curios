package top.theillusivec4.curios.common.network.client;

import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public record CPacketToggleCosmetics(int windowId) implements CustomPacketPayload {

  public static final Type<CPacketToggleCosmetics> TYPE =
      new Type<>(new ResourceLocation(CuriosConstants.MOD_ID, "toggle_cosmetics"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CPacketToggleCosmetics> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.INT, CPacketToggleCosmetics::windowId,
          CPacketToggleCosmetics::new);

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
