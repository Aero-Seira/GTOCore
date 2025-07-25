package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.machine.part.AmountConfigurationHatchPartMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public final class OverclockHatchPartMachine extends AmountConfigurationHatchPartMachine {

    public OverclockHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, 2, tier - 6);
    }

    public double getCurrentMultiplier() {
        return 1D / getCurrent();
    }
}
