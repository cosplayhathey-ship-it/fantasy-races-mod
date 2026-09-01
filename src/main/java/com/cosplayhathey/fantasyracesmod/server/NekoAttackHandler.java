package com.cosplayhathey.fantasyracesmod.server;

import com.cosplayhathey.fantasyracesmod.common.PlayerRaceStorage;
import com.cosplayhathey.fantasyracesmod.common.RaceId;
import com.cosplayhathey.fantasyracesmod.network.NetworkHandler;
import com.cosplayhathey.fantasyracesmod.network.PlayEmotePacket;
import com.cosplayhathey.fantasyracesmod.network.PlayEmoteS2CPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class NekoAttackHandler {
    // Server-side cooldown to prevent spam
    private static final Map<UUID, Long> lastScratchAt = new ConcurrentHashMap<>();
    private static final long SCRATCH_COOLDOWN_MS = 1000L; // 1 second

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();
        if (player.level.isClientSide) return; // only run on server

        // Only trigger when unarmed (main hand empty) and for NEKO/CATFOLK races
        if (!player.getMainHandItem().isEmpty()) return;
        RaceId race = PlayerRaceStorage.getRace(player);
        if (race != RaceId.NEKO && race != RaceId.CATFOLK) return;

        // Only handle if target is a living entity
        if (!(event.getTarget() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getTarget();

        // Cooldown check
        UUID id = player.getUUID();
        long now = System.currentTimeMillis();
        Long last = lastScratchAt.get(id);
        if (last != null && (now - last) < SCRATCH_COOLDOWN_MS) {
            // Cancel default attack to prevent extra hits during cooldown
            event.setCanceled(true);
            return;
        }
        lastScratchAt.put(id, now);

        // Cancel the default attack (we'll apply custom damage)
        event.setCanceled(true);

        // Apply damage: scratch deals 4.0 damage (2 hearts)
        float damage = 4.0f;
        target.hurt(DamageSource.playerAttack(player), damage);

        // Swing animation
        player.swing(InteractionHand.MAIN_HAND);

        // Broadcast emote packet so clients can play the scratch animation
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayEmoteS2CPacket(player.getUUID(), PlayEmotePacket.EmoteType.SCRATCH));
    }
}
