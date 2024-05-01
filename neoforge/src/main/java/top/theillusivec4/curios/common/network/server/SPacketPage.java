package top.theillusivec4.curios.common.network.server;

import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketPage(int windowId, int page) implements CustomPacketPayload {

  public static final Type<SPacketPage> TYPE =
      new Type<>(new ResourceLocation(CuriosConstants.MOD_ID, "server_page"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketPage> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.INT, SPacketPage::windowId, ByteBufCodecs.INT,
          SPacketPage::page, SPacketPage::new);

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
