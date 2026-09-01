package com.cosplayhathey.fantasyracesmod.client.anim;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import net.minecraft.world.entity.player.Player;

/**
 * A lightweight IAnimatable wrapper for Player so we can attach an AnimationFactory and controllers
 * on a per-player basis without modifying the Player class.
 */
public class PlayerAnimatable implements IAnimatable {
    private final Player player;
    private final AnimationFactory factory = new AnimationFactory(this);

    public PlayerAnimatable(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(AnimationData data) {
        // Controllers are created/registered externally in NekoGeoLayer using the factory instance.
    }
}
