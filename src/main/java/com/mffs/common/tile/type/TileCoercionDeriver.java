package com.mffs.common.tile.type;

import com.mffs.SettingConfiguration;
import com.mffs.api.modules.IModule;
import com.mffs.common.items.card.ItemCardFrequency;
import com.mffs.common.items.modules.upgrades.ItemModuleScale;
import com.mffs.common.items.modules.upgrades.ItemModuleSpeed;
import com.mffs.common.tile.TileElectrical;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

/**
 * @author Calclavia
 */
public final class TileCoercionDeriver extends TileElectrical {

    public static final int SLOT_FREQUENCY = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_FUEL = 2;
    public int processTime = 0;
    public boolean isInversed;

    public TileCoercionDeriver() {
        this.capacityBase = 30;
        this.module_index = 3;
        storage.setCapacity(Math.round(getWattage()));
        storage.setMaxTransfer(Math.round(getWattage() / 20L));
    }

    @Override
    public void validate() {
        super.validate();
        start();
    }

    @Override
    public void onSlotsChanged(int... slots) {
        super.onSlotsChanged(slots);
        storage.setCapacity(Math.round(getWattage()));
        storage.setMaxTransfer(Math.round(getWattage() / 20L));
    }

    @Override
    public void start() {
        super.start();
        storage.setCapacity(Math.round(getWattage()));
        storage.setMaxTransfer(Math.round(getWattage() / 20L));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!worldObj.isRemote) {

            if (isActive()) {
                if (isInversed && SettingConfiguration.ENABLE_ELECTRICITY) {
                    if (storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        int produce = (int) Math.floor(requestFortron(getProductionRate() / 20, true) / 0.001);
                        storage.receiveEnergy(produce, false);
                    }
                    //TODO: recharge from battery!
                } else if (getFortronEnergy() < getFortronCapacity()) {
                    //CHeck slot 1 for batteries etc
                    //TODO: Discharge battery
                    if (!SettingConfiguration.ENABLE_ELECTRICITY && isItemValidForSlot(SLOT_FUEL, getStackInSlot(SLOT_FUEL))
                            || storage.extractEnergy(storage.getMaxExtract(), true) >= storage.getMaxExtract()) {
                        provideFortron(getProductionRate(), true);
                        storage.extractEnergy(storage.getMaxExtract(), false);
                        if (processTime == 0 && isItemValidForSlot(SLOT_FUEL, getStackInSlot(SLOT_FUEL))) {
                            decrStackSize(SLOT_FUEL, 1);
                            this.processTime = (200 * Math.max(getModuleCount(ItemModuleScale.class) / 20, 1));
                        }
                        if (processTime > 0) {
                            processTime--;
                        }
                    }
                }
            }
        } else if (isActive()) {
            animation++;
        }
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
    }

    @Override
    public int getSizeInventory() {
        return 6;
    }

    public float getWattage() {
        return (SettingConfiguration.BASE_POWER_REQUIRED + SettingConfiguration.BASE_POWER_REQUIRED * (getModuleCount(ItemModuleSpeed.class) / 8));
    }

    public int getProductionRate() {
        if (isActive()) {
            int production = (int) (getWattage() / 20.0F * 0.001F * SettingConfiguration.FORTRON_PRODUCTION_MULTIPLIER);

            if (this.processTime > 0) {
                production *= 4;
            }
            return production;
        }
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack) {
        if (itemStack != null) {
            if (slotID >= this.module_index) {
                return itemStack.getItem() instanceof IModule;
            }
            switch (slotID) {
                case SLOT_FREQUENCY:
                    return itemStack.getItem() instanceof ItemCardFrequency;
                case SLOT_BATTERY://battery
                    return false;
                case SLOT_FUEL:
                    return itemStack.getItem() == Items.dye && itemStack.getItemDamage() == 4 || itemStack.getItem() == Items.quartz;
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("process", processTime);
        nbt.setBoolean("inverse", isInversed);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        processTime = nbt.getInteger("process");
        isInversed = nbt.getBoolean("inverse");
    }
}
