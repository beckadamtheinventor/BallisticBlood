package com.beckadam.splatterizer.handlers;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import com.beckadam.splatterizer.helpers.ParticleSpawnHelper;
import com.beckadam.splatterizer.particles.ParticleType;

import java.util.ArrayList;

public class MessageParticleHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        final EntityPlayer player = event.player;
        final World world = player.world;

        if (world.isRemote || !(player instanceof EntityPlayerMP)) return;
    }

    public static class MessageParticleFX implements IMessage {
        private ArrayList<ParticleType> types;
        private ArrayList<MessageParticleFX.Particle> particles;

        public MessageParticleFX(ArrayList<ParticleType> _types, ArrayList<Particle> _particles) {
            types = _types;
            particles = _particles;
        }

        public MessageParticleFX() {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int numTypes = buf.readInt();
            int numParticles = buf.readInt();

            types = new ArrayList<>(numTypes);
            for (int i = 0; i < numTypes; i++) {
                int ordinal = buf.readInt();
                types.add(ParticleType.values()[ordinal]);
            }
            particles = new ArrayList<>(numParticles);
            for (int i = 0; i < numParticles; i++) {
                particles.add(
                        createParticle(
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble()
                        )
                );
            }

        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(types.size());
            buf.writeInt(particles.size());

            for (ParticleType type : types) {
                buf.writeInt(type.ordinal());
            }
            for (Particle particle : particles) {
                buf.writeDouble(particle.pos.x);
                buf.writeDouble(particle.pos.y);
                buf.writeDouble(particle.pos.z);
                buf.writeDouble(particle.dir.x);
                buf.writeDouble(particle.dir.y);
                buf.writeDouble(particle.dir.z);
            }
        }

        public static class Handler implements IMessageHandler<MessageParticleFX, IMessage> {
            @Override
            public IMessage onMessage(MessageParticleFX message, MessageContext ctx) {
                if(ctx.side == Side.CLIENT && ForgeConfigHandler.client.enableSplatterParticles) {
                    if (!message.particles.isEmpty() && !message.types.isEmpty()) {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            for (int i=0; i<message.particles.size(); i++) {
                                ParticleSpawnHelper.spawnParticle(
                                        message.types.get(i),
                                        Minecraft.getMinecraft().world,
                                        message.particles.get(i).pos,
                                        message.particles.get(i).dir
                                );
                            }
                        });
                    }
                }
                return null;
            }
        }

        public static class Particle {
            public final Vec3d pos;
            public final Vec3d dir;

            public Particle(Vec3d p, Vec3d d) {
                pos = p;
                dir = d;
            }
            public Particle(double x, double y, double z, double motX, double motY, double motZ) {
                pos = new Vec3d(x, y, z);
                dir = new Vec3d(motX, motY, motZ);
            }
        }

        public static Particle createParticle(double x, double y, double z, double motX, double motY, double motZ) {
            return new Particle(x, y, z, motX, motY, motZ);
        }

        public static Particle createParticle(BlockPos pos, double motX, double motY, double motZ) {
            return new Particle(pos.getX(), pos.getY(), pos.getZ(), motX, motY, motZ);
        }

    }
}
