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

package top.theillusivec4.curios.common.inventory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;

public class CurioStacksHandler implements ICurioStacksHandler {
  private static final ResourceLocation LEGACY_ID = ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "legacy");

  private final ICuriosItemHandler itemHandler;
  private final String identifier;
  private final Map<ResourceLocation, AttributeModifier> modifiers = new HashMap<>();
  private final Set<AttributeModifier> persistentModifiers = new HashSet<>();
  private final Set<AttributeModifier> cachedModifiers = new HashSet<>();
  private final Multimap<AttributeModifier.Operation, AttributeModifier> modifiersByOperation =
      HashMultimap.create();

  private int baseSize;
  private IDynamicStackHandler stackHandler;
  private IDynamicStackHandler cosmeticStackHandler;
  private boolean visible;
  private boolean cosmetic;
  private boolean canToggleRender;
  private ICurio.DropRule dropRule;
  private boolean update;
  private NonNullList<Boolean> renderHandler;

  public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier) {
    this(itemHandler, identifier, 1, true, false, true, ICurio.DropRule.DEFAULT);
  }

  public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier, int size,
                            boolean visible, boolean cosmetic, boolean canToggleRender,
                            ICurio.DropRule dropRule) {
    this.baseSize = size;
    this.visible = visible;
    this.cosmetic = cosmetic;
    this.itemHandler = itemHandler;
    this.identifier = identifier;
    this.canToggleRender = canToggleRender;
    this.dropRule = dropRule;
    this.renderHandler = NonNullList.withSize(size, true);
    this.stackHandler = new DynamicStackHandler(size,
        (index) -> new SlotContext(identifier, itemHandler.getWearer(), index, false,
            this.getRenders().get(index)));
    this.cosmeticStackHandler = new DynamicStackHandler(size,
        (index) -> new SlotContext(identifier, itemHandler.getWearer(), index, true,
            this.getRenders().get(index)));
  }

  @Override
  public IDynamicStackHandler getStacks() {
    this.update();
    return this.stackHandler;
  }

  @Override
  public IDynamicStackHandler getCosmeticStacks() {
    this.update();
    return this.cosmeticStackHandler;
  }

  @Override
  public NonNullList<Boolean> getRenders() {
    this.update();
    return this.renderHandler;
  }

  @Override
  public boolean canToggleRendering() {
    return this.canToggleRender;
  }

  @Override
  public ICurio.DropRule getDropRule() {
    return this.dropRule;
  }

  @Override
  public int getSlots() {
    this.update();
    return this.stackHandler.getSlots();
  }

  @Override
  public int getSizeShift() {
    return 0;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  @Override
  public void grow(int amount) {
    amount = Math.max(0, amount);

    if (amount > 0) {
      this.addLegacyChange(amount);
    }
  }

  @Override
  public void shrink(int amount) {
    amount = Math.max(0, amount);

    if (amount > 0) {
      this.addLegacyChange(Math.min(this.getSlots(), amount) * -1);
    }
  }

  private void addLegacyChange(int shift) {
    AttributeModifier mod = this.getModifiers().get(LEGACY_ID);
    int current = mod != null ? (int) mod.amount() : 0;
    current += shift;
    AttributeModifier newModifier =
        new AttributeModifier(LEGACY_ID, current,
            AttributeModifier.Operation.ADD_VALUE);
    this.modifiers.put(newModifier.id(), newModifier);
    Collection<AttributeModifier> modifiers =
        this.getModifiersByOperation(newModifier.operation());
    modifiers.remove(newModifier);
    modifiers.add(newModifier);
    this.persistentModifiers.remove(newModifier);
    this.persistentModifiers.add(newModifier);
    this.flagUpdate();
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.putInt("SavedBaseSize", this.baseSize);
    compoundNBT.put("Stacks",
        this.stackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));
    compoundNBT.put("Cosmetics",
        this.cosmeticStackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));

    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    compoundNBT.putBoolean("RenderToggle", this.canToggleRender);
    compoundNBT.putString("DropRule", this.dropRule.toString());

    if (!this.persistentModifiers.isEmpty()) {
      ListTag list = new ListTag();

      for (AttributeModifier attributeModifier : this.persistentModifiers) {
        list.add(attributeModifier.save());
      }
      compoundNBT.put("PersistentModifiers", list);
    }

    if (!this.modifiers.isEmpty()) {
      ListTag list = new ListTag();
      this.modifiers.forEach((uuid, modifier) -> {
        if (!this.persistentModifiers.contains(modifier)) {
          list.add(modifier.save());
        }
      });
      compoundNBT.put("CachedModifiers", list);
    }
    return compoundNBT;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {

    if (nbt.contains("SavedBaseSize")) {
      this.baseSize = nbt.getInt("SavedBaseSize");
    }

    if (nbt.contains("Stacks")) {
      this.stackHandler.deserializeNBT(this.itemHandler.getWearer().registryAccess(),
          nbt.getCompound("Stacks"));
    }

    if (nbt.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(this.itemHandler.getWearer().registryAccess(),
          nbt.getCompound("Cosmetics"));
    }

    if (nbt.contains("Renders")) {
      CompoundTag tag = nbt.getCompound("Renders");
      this.renderHandler = NonNullList.withSize(
          nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : this.stackHandler.getSlots(),
          true);
      ListTag tagList = tag.getList("Renders", Tag.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (nbt.contains("SizeShift")) {
      int sizeShift = nbt.getInt("SizeShift");

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic = nbt.contains("HasCosmetic") ? nbt.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = nbt.contains("Visible") ? nbt.getBoolean("Visible") : this.visible;
    this.canToggleRender =
        nbt.contains("RenderToggle") ? nbt.getBoolean("RenderToggle") : this.canToggleRender;

    if (nbt.contains("DropRule")) {
      this.dropRule =
          EnumUtils.getEnum(ICurio.DropRule.class, nbt.getString("DropRule"), this.dropRule);
    }

    if (nbt.contains("PersistentModifiers", 9)) {
      ListTag list = nbt.getList("PersistentModifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.load(list.getCompound(i));

        if (attributeModifier != null) {
          this.addPermanentModifier(attributeModifier);
        }
      }
    }

    if (nbt.contains("CachedModifiers", 9)) {
      ListTag list = nbt.getList("CachedModifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.load(list.getCompound(i));

        if (attributeModifier != null) {
          this.cachedModifiers.add(attributeModifier);
          this.addTransientModifier(attributeModifier);
        }
      }
    }
    this.update();
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  public CompoundTag getSyncTag() {
    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.put("Stacks",
        this.stackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));
    compoundNBT.put("Cosmetics",
        this.cosmeticStackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));

    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    compoundNBT.putBoolean("RenderToggle", this.canToggleRender);
    compoundNBT.putString("DropRule", this.dropRule.toString());
    compoundNBT.putInt("BaseSize", this.baseSize);

    if (!this.modifiers.isEmpty()) {
      ListTag list = new ListTag();

      for (Map.Entry<ResourceLocation, AttributeModifier> modifier : this.modifiers.entrySet()) {
        list.add(modifier.getValue().save());
      }
      compoundNBT.put("Modifiers", list);
    }
    return compoundNBT;
  }

  public void applySyncTag(CompoundTag tag) {

    if (tag.contains("BaseSize")) {
      this.baseSize = tag.getInt("BaseSize");
    }

    if (tag.contains("Stacks")) {
      this.stackHandler.deserializeNBT(this.itemHandler.getWearer().registryAccess(),
          tag.getCompound("Stacks"));
    }

    if (tag.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(this.itemHandler.getWearer().registryAccess(),
          tag.getCompound("Cosmetics"));
    }

    if (tag.contains("Renders")) {
      CompoundTag compoundNBT = tag.getCompound("Renders");
      this.renderHandler = NonNullList.withSize(
          compoundNBT.contains("Size", Tag.TAG_INT) ? compoundNBT.getInt("Size") :
              this.stackHandler.getSlots(), true);
      ListTag tagList = compoundNBT.getList("Renders", Tag.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (tag.contains("SizeShift")) {
      int sizeShift = tag.getInt("SizeShift");

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic = tag.contains("HasCosmetic") ? tag.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = tag.contains("Visible") ? tag.getBoolean("Visible") : this.visible;
    this.canToggleRender =
        tag.contains("RenderToggle") ? tag.getBoolean("RenderToggle") : this.canToggleRender;

    if (tag.contains("DropRule")) {
      this.dropRule =
          EnumUtils.getEnum(ICurio.DropRule.class, tag.getString("DropRule"), this.dropRule);
    }
    this.modifiers.clear();
    this.persistentModifiers.clear();
    this.modifiersByOperation.clear();

    if (tag.contains("Modifiers", 9)) {
      ListTag list = tag.getList("Modifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.load(list.getCompound(i));

        if (attributeModifier != null) {
          this.addTransientModifier(attributeModifier);
        }
      }
    }
    this.flagUpdate();
    this.update();
  }

  @Override
  public void copyModifiers(ICurioStacksHandler other) {
    this.modifiers.clear();
    this.cachedModifiers.clear();
    this.modifiersByOperation.clear();
    this.persistentModifiers.clear();
    other.getModifiers().forEach((uuid, modifier) -> this.addTransientModifier(modifier));
    this.cachedModifiers.addAll(other.getCachedModifiers());

    for (AttributeModifier persistentModifier : other.getPermanentModifiers()) {
      this.addPermanentModifier(persistentModifier);
    }
    this.update();
  }

  public Map<ResourceLocation, AttributeModifier> getModifiers() {
    return this.modifiers;
  }

  @Override
  public Set<AttributeModifier> getPermanentModifiers() {
    return this.persistentModifiers;
  }

  @Override
  public Set<AttributeModifier> getCachedModifiers() {
    return this.cachedModifiers;
  }

  public Collection<AttributeModifier> getModifiersByOperation(
      AttributeModifier.Operation operation) {
    return this.modifiersByOperation.get(operation);
  }

  public void addTransientModifier(AttributeModifier modifier) {
    this.modifiers.put(modifier.id(), modifier);
    this.getModifiersByOperation(modifier.operation()).add(modifier);
    this.flagUpdate();
  }

  public void addPermanentModifier(AttributeModifier modifier) {
    this.addTransientModifier(modifier);
    this.persistentModifiers.add(modifier);
  }

  public void removeModifier(ResourceLocation uuid) {
    AttributeModifier modifier = this.modifiers.remove(uuid);

    if (modifier != null) {
      this.persistentModifiers.remove(modifier);
      this.getModifiersByOperation(modifier.operation()).remove(modifier);
      this.flagUpdate();
    }
  }

  private void flagUpdate() {
    this.update = true;

    if (this.itemHandler != null) {
      this.itemHandler.getUpdatingInventories().remove(this);
      this.itemHandler.getUpdatingInventories().add(this);
    }
  }

  public void clearModifiers() {
    Set<ResourceLocation> ids = new HashSet<>(this.modifiers.keySet());

    for (ResourceLocation id : ids) {
      this.removeModifier(id);
    }
  }

  public void clearCachedModifiers() {

    for (AttributeModifier cachedModifier : this.cachedModifiers) {
      this.removeModifier(cachedModifier.id());
    }
    this.cachedModifiers.clear();
    this.flagUpdate();
  }

  public void update() {

    if (this.update) {
      this.update = false;
      double baseSize = this.baseSize;

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.ADD_VALUE)) {
        baseSize += mod.amount();
      }
      double size = baseSize;

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
        size += this.baseSize * mod.amount();
      }

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
        size *= mod.amount();
      }

      if (size != this.getSlots()) {
        this.resize((int) size);

        if (this.itemHandler != null && this.itemHandler.getWearer() != null) {
          NeoForge.EVENT_BUS.post(
              new SlotModifiersUpdatedEvent(this.itemHandler.getWearer(), Set.of(this.identifier)));

          if (this.itemHandler.getWearer() instanceof Player player &&
              player.containerMenu instanceof ICuriosMenu curiosMenu) {
            curiosMenu.resetSlots();
          }
        }
      }
    }
  }

  private void resize(int newSize) {
    int currentSize = this.getSlots();

    if (currentSize != newSize) {
      int change = newSize - currentSize;

      if (currentSize > newSize) {
        change = change * -1;
        this.loseStacks(this.stackHandler, identifier, change);
        this.stackHandler.shrink(change);
        this.cosmeticStackHandler.shrink(change);
        NonNullList<Boolean> newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
      } else {
        this.stackHandler.grow(change);
        this.cosmeticStackHandler.grow(change);
        NonNullList<Boolean> newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
      }
    }
  }

  private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {

    if (this.itemHandler == null) {
      return;
    }
    List<ItemStack> drops = new ArrayList<>();

    for (int i = Math.max(0, stackHandler.getSlots() - amount);
         i >= 0 && i < stackHandler.getSlots(); i++) {
      ItemStack stack = stackHandler.getStackInSlot(i);
      drops.add(stackHandler.getStackInSlot(i));
      LivingEntity entity = this.itemHandler.getWearer();
      SlotContext slotContext = new SlotContext(identifier, entity, i, false, this.visible);

      if (!stack.isEmpty()) {
        UUID uuid = CuriosApi.getSlotUuid(slotContext);
        Multimap<Holder<Attribute>, AttributeModifier> map =
            CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
        Multimap<String, AttributeModifier> slots = HashMultimap.create();
        Set<Holder<Attribute>> toRemove = new HashSet<>();
        AttributeMap attributeMap = entity.getAttributes();

        for (Holder<Attribute> attribute : map.keySet()) {

          if (attribute.value() instanceof SlotAttribute wrapper) {
            slots.putAll(wrapper.getIdentifier(), map.get(attribute));
            toRemove.add(attribute);
          }
        }

        for (Holder<Attribute> attribute : toRemove) {
          map.removeAll(attribute);
        }

        map.forEach((key, value) -> {
          AttributeInstance attInst = attributeMap.getInstance(key);

          if (attInst != null) {
            attInst.removeModifier(value);
          }
        });
        this.itemHandler.removeSlotModifiers(slots);
        CuriosApi.getCurio(stack).ifPresent(curio -> curio.onUnequip(slotContext, ItemStack.EMPTY));
      }
      stackHandler.setStackInSlot(i, ItemStack.EMPTY);
    }
    drops.forEach(this.itemHandler::loseInvalidStack);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurioStacksHandler that = (CurioStacksHandler) o;
    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }
}
