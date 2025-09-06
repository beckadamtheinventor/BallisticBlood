package com.beckadam.splatterizer.message;

import com.beckadam.splatterizer.handlers.ForgeConfigHandler;
import com.beckadam.splatterizer.helpers.ParticleSpawnHelper;
import com.beckadam.splatterizer.particles.ParticleType;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

// SERVER --> CLIENT
public class MessageParticleHandler {

    public static class MessageParticleFX implements IMessage {
        private ParticleType splatterType;
        private Vec3d splatterPosition;
        private Vec3d splatterDirection;

        public MessageParticleFX(ParticleType type, Vec3d position, Vec3d direction) {
            splatterType = type;
            splatterPosition = position;
            splatterDirection = direction;
        }

        public MessageParticleFX() {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int t = buf.readInt();
            if (t >= 0 && t < ParticleType.values().length) {
                splatterType = ParticleType.values()[t];
            } else {
                splatterType = ParticleType.BLOOD_SPLATTER;
            }
            splatterPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            splatterDirection = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(splatterType.ordinal());
            buf.writeDouble(splatterPosition.x);
            buf.writeDouble(splatterPosition.y);
            buf.writeDouble(splatterPosition.z);
            buf.writeDouble(splatterDirection.x);
            buf.writeDouble(splatterDirection.y);
            buf.writeDouble(splatterDirection.z);
        }

        // CLIENT SIDE
        public static class Handler implements IMessageHandler<MessageParticleFX, IMessage> {
            @Override
            public IMessage onMessage(MessageParticleFX message, MessageContext ctx) {
                if(ctx.side == Side.CLIENT && ForgeConfigHandler.client.enableSplatterParticles) {
                    ParticleSpawnHelper.splatter(
                            message.splatterType,
                            message.splatterPosition,
                            message.splatterDirection
                    );
                }
                return null;
            }
        }

    }
}
