package com.konasclient.konas.module.modules.client;

import baritone.api.BaritoneAPI;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import meteordevelopment.orbit.EventHandler;

public class BaritoneModule extends Module {
    private boolean sync = true;

    private final ListenableSettingDecorator<Boolean> allowBreak = new ListenableSettingDecorator<>("Break", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowPlace = new ListenableSettingDecorator<>("Place", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Float> blockReachDistance = new ListenableSettingDecorator<>("Reach", 4.5F, 6F, 1F, 0.1F, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowSprint = new ListenableSettingDecorator<>("Sprint", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowInventory = new ListenableSettingDecorator<>("Inventory", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowDownward = new ListenableSettingDecorator<>("Downward", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowParkour = new ListenableSettingDecorator<>("Parkour", false, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowParkourAscend = new ListenableSettingDecorator<>("ParkourAscend", false, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> allowParkourPlace = new ListenableSettingDecorator<>("ParkourPlace", false, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> enterPortal = new ListenableSettingDecorator<>("EnterPortals", false, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> freeLook = new ListenableSettingDecorator<>("FreeLook", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Long> failureTimeoutMS = new ListenableSettingDecorator<>("Timeout", 2L, 20L, 1L, 1L, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> renderGoal = new ListenableSettingDecorator<>("Render", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> mineScanDroppedItems = new ListenableSettingDecorator<>("PickupItems", true, (value) -> sync = true);

    private final ListenableSettingDecorator<Boolean> backFill = new ListenableSettingDecorator<>("Backfill", false, (value) -> sync = true);

    public BaritoneModule() {
        super("Baritone", "Baritone settings", 0xFF4A548B, Category.Client);
    }

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (sync) {
            sync = false;
            BaritoneAPI.getSettings().chatControl.value = false;
            BaritoneAPI.getSettings().allowBreak.value = allowBreak.getValue();
            BaritoneAPI.getSettings().allowPlace.value = allowPlace.getValue();
            BaritoneAPI.getSettings().blockReachDistance.value = blockReachDistance.getValue();
            BaritoneAPI.getSettings().allowSprint.value = allowSprint.getValue();
            BaritoneAPI.getSettings().allowInventory.value = allowInventory.getValue();
            BaritoneAPI.getSettings().allowDownward.value = allowDownward.getValue();
            BaritoneAPI.getSettings().allowParkour.value = allowParkour.getValue();
            BaritoneAPI.getSettings().allowParkourAscend.value = allowParkourAscend.getValue();
            BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.getValue();
            BaritoneAPI.getSettings().enterPortal.value = enterPortal.getValue();
            BaritoneAPI.getSettings().freeLook.value = freeLook.getValue();
            BaritoneAPI.getSettings().failureTimeoutMS.value = failureTimeoutMS.getValue() * 1000L;
            BaritoneAPI.getSettings().renderGoal.value = renderGoal.getValue();
            BaritoneAPI.getSettings().mineScanDroppedItems.value = mineScanDroppedItems.getValue();
            BaritoneAPI.getSettings().backfill.value = backFill.getValue();
        }
    }

    @Override
    public void onEnable() {
        sync = true;
        toggle();
    }
}
