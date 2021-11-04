package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.mixin.ClientPlayerEntityAccessor;
import com.konasclient.konas.mixin.PlayerInteractEntityC2SPacketAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Interactions;
import com.konasclient.konas.module.modules.render.Targets;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.setting.SubBind;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.client.TickRateUtil;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.entity.EntityUtils;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.interaction.LookCalculator;
import com.konasclient.konas.util.math.DamageCalculator;
import com.konasclient.konas.util.math.RayTraceHelper;
import com.konasclient.konas.util.math.RoundingUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {
    public static final Setting<Boolean> render = new Setting<>("Render", true);
    public static final Setting<Boolean> debug = new Setting<>("Debug", false);

    public static Setting<Parent> antiCheat = new Setting<>("AntiCheat", new Parent(false));
    public static Setting<TimingMode> timingMode = new Setting<>("Timing", TimingMode.Sequential).withParent(antiCheat).withDescription("Changes how AC is timed");
    public static Setting<Boolean> rotate = new Setting<>("Rotate", true).withParent(antiCheat).withDescription("Spoof rotations server-side");
    public static Setting<Boolean> inhibit = new Setting<>("Inhibit", true).withParent(antiCheat).withDescription("Prevent unnesasary attacks");
    public static Setting<Boolean> limit = new Setting<>("Limit", false).withParent(antiCheat).withDescription("Limit attacks");
    public static Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.Break).withParent(antiCheat).withDescription("Rotate slower");
    public static Setting<Float> yawAngle = new Setting<>("YawAngle", 0.3F, 1F, 0.1F, 0.1F).withParent(antiCheat).withDescription("Maximum angle to rotate by per tick");
    public static Setting<Integer> yawTicks = new Setting<>("YawTicks", 1, 5, 1, 1).withParent(antiCheat).withDescription("Rotate slower by this amount of ticks");
    public static Setting<Boolean> rayTrace = new Setting<>("RayTrace", true).withParent(antiCheat).withDescription("Check if block sides are visible");
    public static Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true).withParent(antiCheat).withDescription("Bypass stricter NCP anticheats like 2b2t");
    public static Setting<Boolean> protocol = new Setting<>("Protocol", false).withParent(antiCheat).withDescription("1.12 Placement");

    public static Setting<Parent> speeds = new Setting<>("Speeds", new Parent(false));
    public static Setting<ConfirmMode> confirm = new Setting<>("Confirm", ConfirmMode.OFF).withParent(speeds).withDescription("Do not place elsewhere until previous placement has been executed");
    public static Setting<Integer> ticksExisted = new Setting<>("TicksExisted", 0, 20, 0, 1).withParent(speeds).withDescription("Tick delay for 2b2t");
    public static Setting<Integer> attackFactor = new Setting<>("AttackFactor", 2, 20, 0, 1).withParent(speeds).withDescription("Attack factor");
    public static Setting<Float> breakSpeed = new Setting<>("BreakSpeed", 20F, 20F, 1F, 0.1F).withParent(speeds).withDescription("Crystal break speed");
    public static Setting<Float> placeSpeed = new Setting<>("PlaceSpeed", 20F, 20F, 1F, 0.1F).withParent(speeds).withDescription("Crystal place speed");
    public static Setting<SyncMode> sync = new Setting<>("Sync", SyncMode.Strict).withParent(speeds).withDescription("Change how breaking and placing is synchronized");

    public static Setting<Parent> ranges = new Setting<>("Ranges", new Parent(false));
    public static Setting<Float> breakRange = new Setting<>("BreakRange", 4.3F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Break range for breaking visible crystals");
    public static Setting<Float> breakWallsRange = new Setting<>("BreakWalls", 1.5F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Break range for breaking crystals through walls");
    public static Setting<Float> placeRange = new Setting<>("PlaceRange", 4F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Place range for visible blocks");
    public static Setting<Float> placeWallsRange = new Setting<>("PlaceWalls", 3F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Place range for placing through walls");

    public static Setting<Parent> swap = new Setting<>("Swap", new Parent(false));
    public static Setting<Boolean> autoSwap = new Setting<>("AutoSwap", false).withParent(swap).withDescription("Auto Swap");
    public static Setting<Float> swapDelay = new Setting<>("SwapDelay", 1F, 20F, 0F, 1F).withParent(swap).withDescription("Delay in ticks for swapping");
    public static Setting<Float> switchDelay = new Setting<>("GhostDelay", 5F, 10F, 0F, 0.5F).withParent(swap).withDescription("Delay for hitting crystals after swapping");
    public static Setting<Boolean> antiWeakness = new Setting<>("AntiWeakness", false).withParent(swap).withDescription("Swap to sword before hitting crystal when weaknessed");

    public static Setting<Parent> targeting = new Setting<>("Targeting", new Parent(false));
    public static Setting<Boolean> onlyOwn = new Setting<>("OnlyOwn", false).withParent(targeting).withDescription("Only break your own crystals");
    public static Setting<Float> maxBreakSelfDamage = new Setting<>("BreakSelfDmg", 6F, 20F, 0F, 0.5F).withParent(targeting).withDescription("Maximum amount of self damage for breaking crystals");
    public static Setting<Boolean> terrainIgnore = new Setting<>("TerrainIgnore", false).withParent(targeting).withDescription("Ignore breakable blocks when doing damage calculations");
    public static Setting<TargetingMode> targetingMode = new Setting<>("Target", TargetingMode.All).withParent(targeting).withDescription("Algorithm to use for selecting targets");
    public static Setting<PriorityMode> priorityMode = new Setting<>("Priority", PriorityMode.Damage).withParent(targeting).withDescription("Algorithm to use for selecting placements");
    public static Setting<Float> enemyRange = new Setting<>("TargetRange", 8F, 15F, 4F, 0.5F).withParent(targeting).withDescription("Range from which to select targets");
    public static Setting<Integer> predictTicks = new Setting<>("Extrapolation", 1, 10, 0, 1).withParent(targeting).withDescription("Predict target motion by this amount of ticks");
    public static Setting<Float> minPlaceDamage = new Setting<>("MinDamage", 6F, 20F, 0F, 0.5F).withParent(targeting).withDescription("Minimum amount of damage for placing crystals");
    public static Setting<Float> maxPlaceSelfDamage = new Setting<>("PlaceSelfDmg", 12F, 20F, 0F, 0.5F).withParent(targeting).withDescription("Maximum amount of self damage for placing crystals");
    public static Setting<Float> faceplaceHealth = new Setting<>("FaceplaceHP", 4F, 20F, 0F, 0.5F).withParent(targeting).withDescription("Health at which to start faceplacing enemies");
    public static Setting<SubBind> forceFaceplace = new Setting<>("Force", new SubBind(GLFW.GLFW_KEY_LEFT_ALT)).withParent(targeting);


    private final Setting<Parent> targets = new Setting<>("Targets", new Parent(false));
    private final Setting<Boolean> players = new Setting<>("Players", true).withParent(targets);
    private final Setting<Boolean> friends = new Setting<>("Friends", false).withParent(targets);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).withParent(targets);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).withParent(targets);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).withParent(targets);

    public static Setting<Parent> pause = new Setting<>("Pause", new Parent(false));
    public static Setting<Boolean> noMineSwitch = new Setting<>("Mining", false).withParent(pause);
    public static Setting<Boolean> noGapSwitch = new Setting<>("Gapping", false).withParent(pause);
    public static Setting<Boolean> rightClickGap = new Setting<>("RightClickGap", false).withVisibility(noGapSwitch::getValue).withParent(pause);
    public static Setting<Boolean> disableWhenKA = new Setting<>("Aura", true).withParent(pause);
    public static Setting<Float> disableUnderHealth = new Setting<>("Health", 2f, 10f, 0f, 0.5f).withParent(pause);

    private enum TimingMode {
        Sequential, Vanilla
    }

    private enum YawStepMode {
        Off, Break, Full
    }

    private enum ConfirmMode {
        OFF, SEMI, FULL
    }

    private enum SyncMode {
        Strict, Merge, Adapt
    }

    private enum TargetingMode {
        All, Health, Nearest
    }

    private enum PriorityMode {
        Damage, Ratio
    }

    public AutoCrystal() {
        super("auto-crystal", "Automatically place and break crystals", 0xFFFF3333, Category.Combat);
    }

    private static final float[] spawnRates = new float[20];
    private static int nextIndex = 0;
    private static long lastSpawn;

    public static ConcurrentHashMap<Integer, Long> silentMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<BlockPos, Long> placeLocations = new ConcurrentHashMap<>();

    private Vec3d rotations;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer noGhostTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private final Timer linearTimer = new Timer();
    private final Timer cacheTimer = new Timer();
    private final Timer scatterTimer = new Timer();
    private BlockPos cachePos = null;

    private boolean lastBroken = false;

    private final Timer inhibitTimer = new Timer();
    private EndCrystalEntity inhibitEntity = null;

    private Vec3d bilateralVec = null;

    private final List<BlockPos> selfPlacePositions = new CopyOnWriteArrayList<>();

    private int ticks;

    private String targetName;
    private final Timer targetTimer = new Timer();

    public void onEnable() {
        lastBroken = false;
        silentMap.clear();
        rotations = null;
        cachePos = null;
        inhibitEntity = null;
        selfPlacePositions.clear();
        ticks = 0;
        bilateralVec = null;
    }

    @EventHandler(priority = 100)
    public void onUpdatePre(UpdateEvent.Pre event) {
        if (timingMode.getValue() == TimingMode.Sequential) return;

        if (check()) {
            if (!generateBreak()) {
                generatePlace();
            }
        }
    }

    @EventHandler()
    public void onUpdatePre(UpdateEvent.Post event) {
        if (timingMode.getValue() == TimingMode.Sequential) return;

        if (check()) {
            if (!generateBreak()) {
                generatePlace();
            }
        }
    }


    @EventHandler(priority = 50)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        placeLocations.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1500) {
                placeLocations.remove(pos);
            }
        });

        ticks--;

        if (timingMode.getValue() == TimingMode.Sequential) {
            if (bilateralVec != null) {
                for (Entity entity : mc.world.getEntities()) {
                    if (entity instanceof EndCrystalEntity && Math.sqrt(entity.squaredDistanceTo(bilateralVec.x, bilateralVec.y, bilateralVec.z)) <= 6) {
                        silentMap.put(entity.getEntityId(), System.currentTimeMillis());
                    }
                }
                bilateralVec = null;
            }

            if (check()) {
                if (!generateBreak()) {
                    generatePlace();
                }
            }
        }

        if (rotate.getValue() && rotations != null) {
            float[] yp = LookCalculator.calculateAngle(rotations);
            if ((yawStep.getValue() == YawStepMode.Break && ActionManager.trailingBreakAction != null) || yawStep.getValue() == YawStepMode.Full) {
                if (ticks > 0) {
                    yp[0] = ((ClientPlayerEntityAccessor) mc.player).getLastYaw();
                    ActionManager.trailingBreakAction = null;
                    ActionManager.trailingPlaceAction = null;
                } else {
                    float yawDiff = MathHelper.wrapDegrees(yp[0] - ((ClientPlayerEntityAccessor) mc.player).getLastYaw());
                    if (Math.abs(yawDiff) > 180 * yawAngle.getValue()) {
                        yp[0] = ((ClientPlayerEntityAccessor) mc.player).getLastYaw() + (yawDiff * ((180 * yawAngle.getValue()) / Math.abs(yawDiff)));
                        ActionManager.trailingBreakAction = null;
                        ActionManager.trailingPlaceAction = null;
                        ticks = yawTicks.getValue();
                    }
                }
            }
            ActionManager.setTrailingRotation(yp);
        }
    }

    private boolean check() {
        if ((noMineSwitch.getValue() && mc.interactionManager.isBreakingBlock()) || (noGapSwitch.getValue() && mc.player.getActiveItem().getItem() instanceof EnchantedGoldenAppleItem) || (mc.player.getHealth() + mc.player.getAbsorptionAmount() < disableUnderHealth.getValue()) || (disableWhenKA.getValue() && ModuleManager.get(AutoCrystal.class).isActive())) {
            return false;
        }

        if (noGapSwitch.getValue() && rightClickGap.getValue() && mc.options.keyUse.isPressed() && mc.player.inventory.getMainHandStack().getItem() instanceof EndCrystalItem) {
            int gappleSlot = -1;

            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStack(l).getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                    gappleSlot = l;
                    break;
                }
            }

            if (gappleSlot != -1 && gappleSlot != mc.player.inventory.selectedSlot && switchTimer.hasPassed(swapDelay.getValue() * 50)) {
                mc.player.inventory.selectedSlot = gappleSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(gappleSlot));
                switchTimer.reset();
                noGhostTimer.reset();
                return false;
            }
        }

        if (!isOffhand() && !(mc.player.inventory.getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (!autoSwap.getValue()) {
                return false;
            } else return getCrystalSlot() != -1;
        }

        return true;
    }

    private void generatePlace() {
        boolean cpvp = mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address.toLowerCase().contains("crystalpvp");
        int adjustedResponseTime = (int) Math.max(100, ((EntityUtils.getPing(mc.player) + 50) / (TickRateUtil.getLatestTickRate() / 20F))) + 150;
        if ((confirm.getValue() != ConfirmMode.FULL || inhibitEntity == null || inhibitEntity.age >= ticksExisted.getValue())) {
            lastBroken = false;
            if ((sync.getValue() != SyncMode.Strict || breakTimer.hasPassed(950F - breakSpeed.getValue() * 50F - EntityUtils.getPing(mc.player))) && placeTimer.hasPassed(1000F - placeSpeed.getValue() * 50F) && (timingMode.getValue() == TimingMode.Sequential || linearTimer.hasPassed(cpvp ? 20 : 0))) {
                if (confirm.getValue() != ConfirmMode.OFF) {
                    if (cachePos != null && !cacheTimer.hasPassed(adjustedResponseTime + 100) && canPlaceCrystal(cachePos)) {
                        BlockHitResult result = handlePlaceRotation(cachePos);
                        ActionManager.trailingPlaceAction = () -> {
                            if (placeCrystal(result)) {
                                placeTimer.reset();
                            }
                        };
                        if (timingMode.getValue() == TimingMode.Vanilla) {
                            ActionManager.trailingPlaceAction.run();
                            ActionManager.trailingPlaceAction = null;
                        }
                        return;
                    }
                }
                List<BlockPos> blocks = findCrystalBlocks();
                if (!blocks.isEmpty()) {
                    BlockPos candidatePos = findPlacePosition(blocks, getTargetsInRange());
                    if (candidatePos != null) {
                        BlockHitResult result = handlePlaceRotation(candidatePos);
                        ActionManager.trailingPlaceAction = () -> {
                            if (placeCrystal(result)) {
                                placeTimer.reset();
                            }
                        };
                    }
                }
            }
        }
    }

    private BlockHitResult handlePlaceRotation(BlockPos pos) {
        Vec3d eyesPos = LookCalculator.getEyesPos(mc.player);

        if (strictDirection.getValue()) {
            Vec3d closestPoint = null;
            Direction closestDirection = null;
            double closestDistance = 999D;

            for (Vec3d point : multiPoint) {
                Vec3d p = new Vec3d(pos.getX() + point.getX(), pos.getY() + point.getY(), pos.getZ() + point.getZ());
                double dist = p.distanceTo(eyesPos);
                if ((dist < closestDistance && closestDirection == null)) {
                    closestPoint = p;
                    closestDistance = dist;
                }

                BlockHitResult result = mc.world.raycast(new RaycastContext(eyesPos, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

                if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                    double visDist = result.getPos().distanceTo(eyesPos);
                    if (closestDirection == null || visDist < closestDistance) {
                        closestDirection = result.getSide();
                        closestDistance = visDist;
                        closestPoint = result.getPos();
                    }
                }
            }

            if (closestPoint != null) {
                if (rotate.getValue()) {
                    rotations = closestPoint;
                }

                return new BlockHitResult(closestPoint, closestDirection == null ? Direction.getFacing(eyesPos.x - closestPoint.x, eyesPos.y - closestPoint.y, eyesPos.z - closestPoint.z) : closestDirection, pos, false);
            }
        }

        if (rayTrace.getValue()) {
            for (Direction direction : Direction.values()) {
                RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d(pos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                        pos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                        pos.getZ() + 0.5 + direction.getVector().getZ() * 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                BlockHitResult result = mc.world.raycast(raycastContext);
                if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                    rotations = result.getPos();
                    return result;
                }
            }
        }

        if (rotate.getValue()) {
            rotations = new Vec3d(pos.getX() + 0.5D,
                    pos.getY() + 1D,
                    pos.getZ() + 0.5D);
        }

        return new BlockHitResult(new Vec3d(pos.getX() + 0.5D,
                pos.getY() + 1D,
                pos.getZ() + 0.5D), Direction.UP, pos, false);
    }

    private boolean generateBreak() {
        List<LivingEntity> targetsInRange = getTargetsInRange();

        int adjustedResponseTime = (int) Math.max(100, ((EntityUtils.getPing(mc.player) + 50) / (TickRateUtil.getLatestTickRate() / 20F))) + 150;

        EndCrystalEntity crystal = findCrystalTarget(targetsInRange, adjustedResponseTime);

        if (crystal != null) {
            if (crystal.age > ticksExisted.getValue() - 1) {
                if (rotate.getValue()) {
                    rotations = crystal.getPos();
                }
                if (breakTimer.hasPassed(1020F - breakSpeed.getValue() * 50F)) {
                    if (lastBroken) {
                        lastBroken = false;
                        if (sync.getValue() == SyncMode.Strict) {
                            return false;
                        }
                    }
                    ActionManager.trailingBreakAction = () -> {
                        if (breakCrystal(crystal)) {
                            lastBroken = true;
                            breakTimer.reset();
                            silentMap.put(crystal.getEntityId(), System.currentTimeMillis());
                            for (Entity entity : mc.world.getEntities()) {
                                if (entity instanceof EndCrystalEntity && entity.distanceTo(crystal) <= 6) {
                                    silentMap.put(entity.getEntityId(), System.currentTimeMillis());
                                }
                            }
                        }
                        if (sync.getValue() != SyncMode.Strict) {
                            generatePlace();
                        }
                    };
                    if (timingMode.getValue() == TimingMode.Vanilla) {
                        ActionManager.trailingBreakAction.run();
                        ActionManager.trailingBreakAction = null;
                    }
                }
            }
            return true;
        }

        return false;
    }

    public boolean placeCrystal(BlockHitResult result) {
        if (result != null) {
            if (autoSwap.getValue()) {
                if (switchTimer.hasPassed(swapDelay.getValue() * 50)) {
                    if (!setCrystalSlot()) return false;
                } else {
                    return false;
                }
            }

            if (!isOffhand() && mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) {
                return false;
            }

            if (ModuleManager.get(Interactions.class).isActive()) {
                Interactions.Placement placement = new Interactions.Placement(result.getBlockPos(), result.getSide());
                Interactions.crystalPlacements.remove(placement);
                Interactions.crystalPlacements.add(placement);
            }

            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND, result));
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND));

            placeLocations.put(result.getBlockPos(), System.currentTimeMillis());
            selfPlacePositions.add(result.getBlockPos());
            return true;
        }
        return false;
    }

    private boolean breakCrystal(EndCrystalEntity targetCrystal) {
        if (!noGhostTimer.hasPassed(switchDelay.getValue() * 100F)) return false;
        if (targetCrystal != null) {
            if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS) && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                setSwordSlot();
                return false;
            }

            mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(targetCrystal, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            if (inhibit.getValue()) {
                inhibitTimer.reset();
                inhibitEntity = targetCrystal;
            }
            return true;
        }
        return false;
    }

    private BlockPos findPlacePosition(List<BlockPos> blocks, List<LivingEntity> targets) {
        if (!scatterTimer.hasPassed(ticksExisted.getValue() * 50)) {
            return null;
        }

        BlockPos bestPos = null;

        LivingEntity bestTarget = null;

        // Damage targeting
        float bestDamage = 0.0F;

        // Ratio targeting
        float bestRatio = 0.0F;

        if (targets.isEmpty()) return null;

        for (BlockPos block : blocks) {
            Vec3d blockVec = new Vec3d(block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5);
            float damage = 0.0F;
            LivingEntity target = null;
            float damageToSelf = DamageCalculator.getExplosionDamage(blockVec, 6F, mc.player);

            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= damageToSelf + 2F) {
                continue;
            }

            if (damageToSelf > maxPlaceSelfDamage.getValue()) {
                continue;
            }

            for (LivingEntity player : targets) {
                boolean localOverrideMinDamage = false;

                float damageToTarget = DamageCalculator.getExplosionDamage(blockVec, 6F, player);

                if (damageToTarget >= 0.5D) {
                    if (player.getHealth() + player.getAbsorptionAmount() - damageToTarget <= 0 || player.getHealth() + player.getAbsorptionAmount() < faceplaceHealth.getValue()) {
                        localOverrideMinDamage = true;
                    }
                }

                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), forceFaceplace.getValue().getKeyCode())) {
                    localOverrideMinDamage = true;
                }

                if (damageToTarget > damage && (damageToTarget >= minPlaceDamage.getValue() || localOverrideMinDamage)) {
                    damage = damageToTarget;
                    target = player;
                }
            }

            if (priorityMode.getValue() == PriorityMode.Damage) {
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestPos = block;
                    bestTarget = target;
                }
            } else {
                if (damage / damageToSelf > bestRatio) {
                    bestDamage = damage;
                    bestRatio = damage / damageToSelf;
                    bestPos = block;
                    bestTarget = target;
                }
            }
        }

        if (bestTarget != null && bestPos != null) {
            Targets.addTarget(bestTarget);
            targetName = bestTarget.getEntityName();
            targetTimer.reset();
        } else {
            targetName = null;
        }

        cachePos = bestPos;
        cacheTimer.reset();

        return bestPos;
    }

    @Override
    public String getMetadata() {
        if (targetTimer.hasPassed(2500) || targetName == null) return "";
        return targetName;
    }

    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = (x1 - x2);
        double d1 = (y1 - y2);
        double d2 = (z1 - z2);
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static float getCPS() {
        float numSpawns = 0.0F;
        float sumSpawnsRates = 0.0F;
        for (float spawnRate : spawnRates) {
            if (spawnRate > 0.0F) {
                sumSpawnsRates += spawnRate;
                numSpawns += 1.0F;
            }
        }
        if (numSpawns == 0F) return 0F;
        // return MathHelper.clamp((int) Math.ceil((20F / ((sumSpawnsRates / numSpawns) / 50F)) + 0.5F), 0, 10);
        return RoundingUtil.roundFloat(20F / ((sumSpawnsRates / numSpawns) / 50F), 1);
    }

    @EventHandler
    public void onPacketRecive(PacketEvent.Receive event) {
        if (event.packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet = (EntitySpawnS2CPacket) event.packet;
            if (packet.getEntityTypeId() == EntityType.END_CRYSTAL) {
                placeLocations.forEach((pos, time) -> {
                    if (getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, packet.getX(), packet.getY() - 1, packet.getZ()) < 1) {
                        if (lastSpawn != -1L) {
                            float timeElapsed = (float) (System.currentTimeMillis() - lastSpawn);
                            spawnRates[(nextIndex % spawnRates.length)] = timeElapsed;
                            nextIndex += 1;
                        }
                        lastSpawn = System.currentTimeMillis();
                        placeLocations.remove(pos);
                        cachePos = null;
                        if (!limit.getValue() && inhibit.getValue()) {
                            scatterTimer.reset();
                        }

                        if (ticksExisted.getValue() != 0 || mc.player.hasStatusEffect(StatusEffects.WEAKNESS) || !ThreadUtils.isThreadSafe())
                            return;

                        if (!noGhostTimer.hasPassed(switchDelay.getValue() * 100F)) return;

                        if (silentMap.containsKey(packet.getId())) return;

                        if (!check()) return;

                        Vec3d spawnVec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

                        if (LookCalculator.getEyesPos(mc.player).distanceTo(spawnVec) > breakRange.getValue()) return;

                        if (!(breakTimer.hasPassed(1000F - breakSpeed.getValue() * 50F))) return;

                        if (DamageCalculator.getExplosionDamage(spawnVec, 6F, mc.player) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                            return;

                        silentMap.put(packet.getId(), System.currentTimeMillis());
                        bilateralVec = spawnVec;

                        PlayerInteractEntityC2SPacket attackPacket = new PlayerInteractEntityC2SPacket();
                        ((PlayerInteractEntityC2SPacketAccessor) attackPacket).setEntityId(packet.getId());
                        ((PlayerInteractEntityC2SPacketAccessor) attackPacket).setType(PlayerInteractEntityC2SPacket.InteractionType.ATTACK);
                        ((PlayerInteractEntityC2SPacketAccessor) attackPacket).setPlayerSneaking(mc.player.isSneaking());
                        mc.player.networkHandler.sendPacket(attackPacket);
                        mc.player.swingHand(Hand.MAIN_HAND);

                        breakTimer.reset();
                        linearTimer.reset();

                        lastBroken = true;

                        if (sync.getValue() == SyncMode.Adapt) {
                            generatePlace();
                        }
                    }
                });
            }
        }
    }

    private List<BlockPos> findCrystalBlocks() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (canPlaceCrystal(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    public static final Vec3d[] multiPoint = new Vec3d[]{
            // z
            new Vec3d(0.05, 0.05, 0),
            new Vec3d(0.05, 0.95, 0),
            new Vec3d(0.95, 0.05, 0),
            new Vec3d(0.95, 0.95, 0),
            new Vec3d(0.5, 0.5, 0),
            new Vec3d(0.05, 0.05, 1),
            new Vec3d(0.05, 0.95, 1),
            new Vec3d(0.95, 0.05, 1),
            new Vec3d(0.95, 0.95, 1),
            new Vec3d(0.5, 0.5, 1),
            // y
            new Vec3d(0.05, 0, 0.05),
            new Vec3d(0.05, 0, 0.95),
            new Vec3d(0.95, 0, 0.05),
            new Vec3d(0.95, 0, 0.95),
            new Vec3d(0.5, 0, 0.5),
            new Vec3d(0.05, 1, 0.05),
            new Vec3d(0.05, 1, 0.95),
            new Vec3d(0.95, 1, 0.05),
            new Vec3d(0.95, 1, 0.95),
            new Vec3d(0.5, 1, 0.5),
            // x
            new Vec3d(0, 0.05, 0.05),
            new Vec3d(0, 0.95, 0.05),
            new Vec3d(0, 0.05, 0.95),
            new Vec3d(0, 0.95, 0.95),
            new Vec3d(0, 0.5, 0.5),
            new Vec3d(1, 0.05, 0.05),
            new Vec3d(1, 0.95, 0.05),
            new Vec3d(1, 0.05, 0.95),
            new Vec3d(1, 0.95, 0.95),
            new Vec3d(1, 0.5, 0.5)
    };

    public static final Vec3d[] fastMultiPoint = new Vec3d[]{
            new Vec3d(0.05, 0.05, 0.05),
            new Vec3d(0.05, 0.05, 0.95),
            new Vec3d(0.05, 0.95, 0.05),
            new Vec3d(0.95, 0.05, 0.05),
            new Vec3d(0.95, 0.95, 0.05),
            new Vec3d(0.05, 0.95, 0.95),
            new Vec3d(0.95, 0.95, 0.95),
            new Vec3d(0.95, 0.05, 0.95)
    };

    public boolean canPlaceCrystal(BlockPos blockPos) {
        if (!(mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) return false;

        BlockPos boost = blockPos.add(0, 1, 0);

        if (!(mc.world.getBlockState(boost).getBlock() == Blocks.AIR)) return false;

        BlockPos boost2 = blockPos.add(0, 2, 0);

        if (AutoCrystal.protocol.getValue()) {
            if (!(mc.world.getBlockState(boost2).getBlock() == Blocks.AIR)) {
                return false;
            }
        }

        if (!RayTraceHelper.canSee(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.7, blockPos.getZ() + 0.5), new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5))) {
            if (LookCalculator.getEyesPos(mc.player).distanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5)) > breakWallsRange.getValue()) {
                return false;
            }
        }

        Vec3d playerEyes = LookCalculator.getEyesPos(mc.player);
        boolean canPlace = false;

        if (strictDirection.getValue()) {
            for (Vec3d point : fastMultiPoint) {
                Vec3d p = new Vec3d(blockPos.getX() + point.getX(), blockPos.getY() + point.getY(), blockPos.getZ() + point.getZ());
                double distanceTo = playerEyes.distanceTo(p);
                if (distanceTo > placeRange.getValue()) {
                    continue;
                }
                if (distanceTo > placeWallsRange.getValue()) {
                    if (strictDirection.getValue()) {
                        BlockHitResult result = mc.world.raycast(new RaycastContext(playerEyes, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                            canPlace = true;
                            break;
                        }
                    }
                } else {
                    canPlace = true;
                    break;
                }
            }
        } else {
            for (Direction dir : Direction.values()) {
                Vec3d p = new Vec3d(blockPos.getX() + 0.5 + dir.getVector().getX() * 0.5,
                        blockPos.getY() + 0.5 + dir.getVector().getY() * 0.5,
                        blockPos.getZ() + 0.5 + dir.getVector().getZ() * 0.5);
                double distanceTo = playerEyes.distanceTo(p);
                if (distanceTo > placeRange.getValue()) {
                    continue;
                }
                if (distanceTo > placeWallsRange.getValue()) {
                    if (strictDirection.getValue()) {
                        BlockHitResult result = mc.world.raycast(new RaycastContext(playerEyes, p, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                            canPlace = true;
                            break;
                        }
                    }
                } else {
                    canPlace = true;
                    break;
                }
            }
        }

        if (!canPlace) {
            return false;
        }

        return mc.world.getOtherEntities(null, new Box(blockPos).stretch(0, protocol.getValue() ? 2 : 1, 0)).stream()
                .filter(entity -> !silentMap.containsKey(entity.getEntityId()) && (!(entity instanceof EndCrystalEntity) || entity.age > 20)).count() == 0;
    }

    public boolean setCrystalSlot() {
        if (isOffhand()) {
            return true;
        }
        int crystalSlot = getCrystalSlot();
        if (crystalSlot == -1) {
            return false;
        } else if (mc.player.inventory.selectedSlot != crystalSlot) {
            mc.player.inventory.selectedSlot = crystalSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
            switchTimer.reset();
            noGhostTimer.reset();
        }
        return true;
    }

    public void setSwordSlot() {
        int swordSlot = getSwordSlot();
        if (mc.player.inventory.selectedSlot != swordSlot && swordSlot != -1) {
            mc.player.inventory.selectedSlot = swordSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swordSlot));
            switchTimer.reset();
            noGhostTimer.reset();
        }
    }

    private int getSwordSlot() {
        int swordSlot = -1;

        if (mc.player.getMainHandStack().getItem() == Items.DIAMOND_SWORD) {
            swordSlot = mc.player.inventory.selectedSlot;
        }

        if (swordSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStack(l).getItem() == Items.DIAMOND_SWORD) {
                    swordSlot = l;
                    break;
                }
            }
        }

        return swordSlot;
    }

    private EndCrystalEntity findCrystalTarget(List<LivingEntity> targetsInRange, int adjustedResponseTime) {
        silentMap.forEach((id, time) -> {
            if (System.currentTimeMillis() - time > 1000) {
                silentMap.remove(id);
            }
        });

        EndCrystalEntity bestCrystal = null;

        if (inhibit.getValue() && !limit.getValue() && !inhibitTimer.hasPassed(adjustedResponseTime) && inhibitEntity != null) {
            if (mc.world.getEntityById(inhibitEntity.getEntityId()) != null && isValidCrystalTarget(inhibitEntity)) {
                bestCrystal = inhibitEntity;
                return bestCrystal;
            }
        }

        List<EndCrystalEntity> crystalsInRange = getCrystalInRange();

        if (crystalsInRange.isEmpty()) {
            return null;
        }

        double bestDamage = 0.0D;

        for (EndCrystalEntity crystal : crystalsInRange) {
            if (crystal.getPos().distanceTo(LookCalculator.getEyesPos(mc.player)) < breakWallsRange.getValue() || RayTraceHelper.canSee(crystal)) {

                double selfDamage = DamageCalculator.getExplosionDamage(crystal, mc.player);

                if (!selfPlacePositions.contains(new BlockPos(crystal.getX(), crystal.getY() - 1, crystal.getZ())) && selfDamage > maxBreakSelfDamage.getValue()) {
                    continue;
                }

                double damage = 0.0D;

                for (LivingEntity target : targetsInRange) {
                    double targetDamage = DamageCalculator.getExplosionDamage(crystal, target);
                    damage += targetDamage;
                }

                if (onlyOwn.getValue()) {
                    if (!selfPlacePositions.contains(new BlockPos(crystal.getX(), crystal.getY() - 1, crystal.getZ()))) {
                        continue;
                    }
                } else {
                    if (!selfPlacePositions.contains(new BlockPos(crystal.getX(), crystal.getY() - 1, crystal.getZ())) && (damage < minPlaceDamage.getValue() || damage < selfDamage))
                        continue;
                }

                if (damage > bestDamage || bestDamage == 0D) {
                    bestDamage = damage;
                    bestCrystal = crystal;
                }
            }
        }

        return bestCrystal;
    }

    private List<EndCrystalEntity> getCrystalInRange() {
        List<EndCrystalEntity> list = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (!isValidCrystalTarget((EndCrystalEntity) entity)) continue;
            list.add((EndCrystalEntity) entity);
        }

        return list;
    }

    private boolean isValidCrystalTarget(EndCrystalEntity crystal) {
        if (LookCalculator.getEyesPos(mc.player).distanceTo(crystal.getPos()) > breakRange.getValue()) return false;
        if (silentMap.containsKey(crystal.getEntityId()) && limit.getValue()) return false;
        if (silentMap.containsKey(crystal.getEntityId()) && crystal.age > ticksExisted.getValue() + attackFactor.getValue())
            return false;
        return !(DamageCalculator.getExplosionDamage(crystal, mc.player) + 2F >= mc.player.getHealth() + mc.player.getAbsorptionAmount());
    }

    private List<LivingEntity> getTargetsInRange() {
        List<LivingEntity> list = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (!shouldTarget(entity)) continue;
            if (entity.distanceTo(mc.player) > enemyRange.getValue()) continue;
            if (((LivingEntity) entity).isDead()) continue;
            if (((LivingEntity) entity).getHealth() + ((LivingEntity) entity).getAbsorptionAmount() <= 0) continue;
            list.add((LivingEntity) entity);
        }

        if (targetingMode.getValue() == TargetingMode.All) return list;
        else if (targetingMode.getValue() == TargetingMode.Health) {
            return list.stream().sorted(Comparator.comparing(e -> (e.getHealth() + e.getAbsorptionAmount()))).limit(1).collect(Collectors.toList());
        } else {
            return list.stream().sorted(Comparator.comparing(e -> (e.distanceTo(mc.player)))).limit(1).collect(Collectors.toList());
        }
    }

    private boolean shouldTarget(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (entity == mc.player) return false;

            if (Friends.isFriend(entity.getName().asString())) {
                return friends.getValue();
            }

            return players.getValue();
        }

        switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_AMBIENT:
            case WATER_CREATURE:
                return creatures.getValue();
            case MONSTER:
                return monsters.getValue();
            case AMBIENT:
                return ambients.getValue();
            default:
                return false;
        }
    }

    private boolean isOffhand() {
        return mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }

    private int getCrystalSlot() {
        int crystalSlot = -1;

        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            crystalSlot = mc.player.inventory.selectedSlot;
        }


        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStack(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }

        return crystalSlot;
    }
}
