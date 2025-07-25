package com.gtocore.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class FluidRegulatorCover extends ULVPumpCover {

    private static final int MAX_STACK_SIZE = 2048000000;
    @Persisted
    @DescSynced
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;
    @Persisted
    @DescSynced
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;
    @Persisted
    @DescSynced
    private int globalTransferSizeMillibuckets;
    private int fluidTransferBuffered = 0;
    private NumberInputWidget<Integer> transferSizeInput;
    private EnumSelectorWidget<BucketMode> transferBucketModeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    //////////////////////////////////////
    // ***** Transfer Logic ******//
    //////////////////////////////////////
    @Override
    protected int doTransferFluidsInternal(@NotNull IFluidHandlerModifiable source, @NotNull IFluidHandlerModifiable destination, int platformTransferLimit) {
        return switch (transferMode) {
            case TRANSFER_ANY -> transferAny(source, destination, platformTransferLimit);
            case TRANSFER_EXACT -> transferExact(source, destination, platformTransferLimit);
            case KEEP_EXACT -> keepExact(source, destination, platformTransferLimit);
        };
    }

    private int transferExact(IFluidHandler source, IFluidHandler destination, int platformTransferLimit) {
        int fluidLeftToTransfer = platformTransferLimit;
        for (int slot = 0; slot < source.getTanks(); slot++) {
            if (fluidLeftToTransfer <= 0) break;
            FluidStack sourceFluid = source.getFluidInTank(slot).copy();
            int supplyAmount = getFilteredFluidAmount(sourceFluid);
            // If the remaining transferrable amount in this operation is not enough to transfer the full stack size,
            // the remaining amount for this operation will be buffered and added to the next operation's maximum.
            if (fluidLeftToTransfer + fluidTransferBuffered < supplyAmount) {
                this.fluidTransferBuffered += fluidLeftToTransfer;
                fluidLeftToTransfer = 0;
                break;
            }
            if (sourceFluid.isEmpty() || supplyAmount <= 0) continue;
            sourceFluid.setAmount(supplyAmount);
            FluidStack drained = source.drain(sourceFluid, FluidAction.SIMULATE);
            if (drained.isEmpty() || drained.getAmount() < supplyAmount) continue;
            int insertableAmount = destination.fill(drained.copy(), FluidAction.SIMULATE);
            if (insertableAmount <= 0) continue;
            drained.setAmount(insertableAmount);
            drained = source.drain(drained, FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                destination.fill(drained, FluidAction.EXECUTE);
                fluidLeftToTransfer -= (drained.getAmount() - fluidTransferBuffered);
            }
            fluidTransferBuffered = 0;
        }
        return platformTransferLimit - fluidLeftToTransfer;
    }

    private int keepExact(IFluidHandlerModifiable source, IFluidHandlerModifiable destination, int platformTransferLimit) {
        int fluidLeftToTransfer = platformTransferLimit;
        final Map<FluidStack, Integer> sourceAmounts = enumerateDistinctFluids(source, TransferDirection.EXTRACT);
        final Map<FluidStack, Integer> destinationAmounts = enumerateDistinctFluids(destination, TransferDirection.INSERT);
        for (FluidStack fluidStack : sourceAmounts.keySet()) {
            if (fluidLeftToTransfer <= 0) break;
            int amountToKeep = getFilteredFluidAmount(fluidStack);
            int amountInDest = destinationAmounts.getOrDefault(fluidStack, 0);
            if (amountInDest >= amountToKeep) continue;
            FluidStack fluidToMove = fluidStack.copy();
            fluidToMove.setAmount(Math.min(fluidLeftToTransfer, amountToKeep - amountInDest));
            if (fluidToMove.getAmount() <= 0) continue;
            FluidStack drained = source.drain(fluidToMove, FluidAction.SIMULATE);
            int fillableAmount = destination.fill(drained, FluidAction.SIMULATE);
            if (fillableAmount <= 0) continue;
            fluidToMove.setAmount(Math.min(fluidToMove.getAmount(), fillableAmount));
            drained = source.drain(fluidToMove, FluidAction.EXECUTE);
            int movedAmount = destination.fill(drained, FluidAction.EXECUTE);
            fluidLeftToTransfer -= movedAmount;
        }
        return platformTransferLimit - fluidLeftToTransfer;
    }

    private void setTransferBucketMode(BucketMode transferBucketMode) {
        var oldMultiplier = this.transferBucketMode.multiplier;
        var newMultiplier = transferBucketMode.multiplier;
        this.transferBucketMode = transferBucketMode;
        if (transferSizeInput == null) return;
        if (oldMultiplier > newMultiplier) {
            transferSizeInput.setValue(getCurrentBucketModeTransferSize());
        }
        this.transferSizeInput.setMax(MAX_STACK_SIZE / this.transferBucketMode.multiplier);
        if (newMultiplier > oldMultiplier) {
            transferSizeInput.setValue(getCurrentBucketModeTransferSize());
        }
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        configureTransferSizeInput();
        if (!this.isRemote()) {
            configureFilter();
        }
    }

    @Override
    void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1 : MAX_STACK_SIZE);
        }
        configureTransferSizeInput();
    }

    private int getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent()) return globalTransferSizeMillibuckets;
        FluidFilter filter = filterHandler.getFilter();
        return (filter.supportsAmounts() ? filter.testFluidAmount(fluidStack) : globalTransferSizeMillibuckets);
    }

    ///////////////////////////
    // ***** GUI ******//
    ///////////////////////////
    @Override
    @NotNull
    protected String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    protected void buildAdditionalUI(@NotNull WidgetGroup group) {
        group.addWidget(new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));
        this.transferSizeInput = new IntInputWidget(35, 45, 84, 20, this::getCurrentBucketModeTransferSize, this::setCurrentBucketModeTransferSize).setMin(0).setMax(Integer.MAX_VALUE);
        configureTransferSizeInput();
        group.addWidget(this.transferSizeInput);
        this.transferBucketModeInput = new EnumSelectorWidget<>(121, 45, 20, 20, BucketMode.values(), transferBucketMode, this::setTransferBucketMode);
        group.addWidget(this.transferBucketModeInput);
    }

    private int getCurrentBucketModeTransferSize() {
        return this.globalTransferSizeMillibuckets / this.transferBucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(int transferSize) {
        this.globalTransferSizeMillibuckets = Math.min(Math.max(transferSize * this.transferBucketMode.multiplier, 0), MAX_STACK_SIZE);
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null || transferBucketModeInput == null) return;
        this.transferSizeInput.setVisible(shouldShowTransferSize());
        this.transferBucketModeInput.setVisible(shouldShowTransferSize());
    }

    private boolean shouldShowTransferSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY) return false;
        if (!this.filterHandler.isFilterPresent()) return true;
        return !this.filterHandler.getFilter().supportsAmounts();
    }

    //////////////////////////////////////
    // ***** LDLib SyncData ******//
    //////////////////////////////////////
    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidRegulatorCover.class, ULVPumpCover.MANAGED_FIELD_HOLDER);

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public TransferMode getTransferMode() {
        return this.transferMode;
    }

    public BucketMode getTransferBucketMode() {
        return this.transferBucketMode;
    }

    public int getGlobalTransferSizeMillibuckets() {
        return this.globalTransferSizeMillibuckets;
    }
}
