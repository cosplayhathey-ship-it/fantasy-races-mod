package com.cosplayhathey.fantasyracesmod.parent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ParentManager manages parent-child links and hydrates/persists them to player NBT.
 * It exposes methods to link/unlink parents and to query parents for a child.
 */
public class ParentManager {
    public static final String PARENTS_TAG = "fr:parents";
    private static final ParentManager INSTANCE = new ParentManager();

    // child UUID -> list of parent UUIDs
    private final Map<UUID, List<UUID>> childToParents = new ConcurrentHashMap<>();

    private ParentManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ParentManager getInstance() {
        return INSTANCE;
    }

    public List<UUID> getParents(UUID child) {
        return childToParents.getOrDefault(child, Collections.emptyList());
    }

    public void linkParent(ServerPlayer childPlayer, Entity parentEntity) {
        if (childPlayer == null || parentEntity == null) return;
        UUID child = childPlayer.getUUID();
        UUID p = parentEntity.getUUID();
        childToParents.compute(child, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            if (!v.contains(p)) v.add(p);
            return v;
        });
        persistParentsToPlayer(childPlayer);
        childPlayer.sendMessage(net.minecraft.network.chat.Component.literal("Linked parent " + parentEntity.getName().getString() + " to you."), childPlayer.getUUID());
    }

    public void unlinkParent(ServerPlayer childPlayer, Entity parentEntity) {
        if (childPlayer == null || parentEntity == null) return;
        UUID child = childPlayer.getUUID();
        UUID p = parentEntity.getUUID();
        childToParents.computeIfPresent(child, (k, v) -> {
            v.remove(p);
            return v.isEmpty() ? null : v;
        });
        persistParentsToPlayer(childPlayer);
        childPlayer.sendMessage(net.minecraft.network.chat.Component.literal("Unlinked parent " + parentEntity.getName().getString()), childPlayer.getUUID());
    }

    public void persistParentsToPlayer(ServerPlayer player) {
        if (player == null) return;
        CompoundTag tag = player.getPersistentData();
        List<UUID> list = childToParents.get(player.getUUID());
        ListTag out = new ListTag();
        if (list != null) {
            for (UUID u : list) out.add(StringTag.valueOf(u.toString()));
        }
        tag.put(PARENTS_TAG, out);
    }

    public void hydrateParentsFromPlayer(ServerPlayer player) {
        if (player == null) return;
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(PARENTS_TAG)) return;
        ListTag in = tag.getList(PARENTS_TAG, 8); // 8 = String
        List<UUID> list = new ArrayList<>();
        for (int i = 0; i < in.size(); i++) {
            try {
                UUID u = UUID.fromString(in.getString(i));
                list.add(u);
            } catch (Exception ignored) {}
        }
        if (!list.isEmpty()) childToParents.put(player.getUUID(), list);
    }

    public void linkNearestParents(ServerPlayer childPlayer, int maxParents, double radius) {
        if (childPlayer == null) return;
        ServerLevel level = childPlayer.getLevel();
        AABB box = childPlayer.getBoundingBox().inflate(radius);
        List<Entity> entities = level.getEntities(null, box, e -> e instanceof Mob && !(e instanceof Player));
        int linked = 0;
        for (Entity e : entities) {
            if (linked >= maxParents) break;
            linkParent(childPlayer, e);
            linked++;
        }
    }

    // Event handlers to persist/hydrate on login and entity join
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        if (ev.getPlayer() instanceof ServerPlayer sp) {
            hydrateParentsFromPlayer(sp);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent ev) {
        // if a player joins world on server side ensure hydration
        if (ev.getEntity() instanceof ServerPlayer sp) {
            hydrateParentsFromPlayer(sp);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent ev) {
        // If a child is hurt, find parents and instruct them to protect
        if (ev.getEntity() instanceof ServerPlayer child) {
            List<UUID> parents = getParents(child.getUUID());
            if (parents.isEmpty()) return;
            // find parent entities in world
            for (UUID pu : parents) {
                Entity p = ((ServerLevel) child.getLevel()).getEntity(pu);
                if (p instanceof PathfinderMob parentMob) {
                    // instruct parent to target attacker if present
                    if (ev.getSource() != null && ev.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
                        parentMob.setTarget(attacker);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent ev) {
        // call periodic parent behaviors (simple approach: iterate known children)
        if (ev.phase != TickEvent.Phase.END) return;
        try {
            for (UUID childId : new ArrayList<>(childToParents.keySet())) {
                // try to find player
                for (ServerLevel lvl : net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                    Entity e = lvl.getEntity(childId);
                    if (e instanceof ServerPlayer childPlayer) {
                        List<UUID> parents = childToParents.get(childId);
                        if (parents == null) continue;
                        for (UUID pu : parents) {
                            Entity pEntity = lvl.getEntity(pu);
                            if (pEntity instanceof PathfinderMob parentMob) {
                                ParentBehavior.performPeriodicCare(parentMob, childPlayer);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // log and continue
            // avoid spamming logs
            // t.printStackTrace();
        }
    }
}
