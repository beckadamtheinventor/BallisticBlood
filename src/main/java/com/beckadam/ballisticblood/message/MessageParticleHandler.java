package com.beckadam.ballisticblood.message;

import com.beckadam.ballisticblood.handlers.ForgeConfigHandler;
import com.beckadam.ballisticblood.helpers.ClientHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

// SERVER --> CLIENT
public class MessageParticleHandler {

    public static class MessageParticle implements IMessage {
        private int splatterType;
        private Vec3d splatterPosition;
        private Vec3d splatterDirection;
        private float splatterDamage;

        public MessageParticle(int type, Vec3d position, Vec3d direction, float damage) {
            splatterType = type;
            splatterPosition = position;
            splatterDirection = direction;
            splatterDamage = damage;
        }

        public MessageParticle() {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            splatterType = buf.readInt();
            splatterDamage = buf.readFloat();
            splatterPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            splatterDirection = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(splatterType);
            buf.writeFloat(splatterDamage);
            buf.writeDouble(splatterPosition.x);
            buf.writeDouble(splatterPosition.y);
            buf.writeDouble(splatterPosition.z);
            buf.writeDouble(splatterDirection.x);
            buf.writeDouble(splatterDirection.y);
            buf.writeDouble(splatterDirection.z);
        }

        // CLIENT SIDE
        public static class Handler implements IMessageHandler<MessageParticle, IMessage> {
            @Override
            public IMessage onMessage(MessageParticle message, MessageContext ctx) {
                if(ctx.side == Side.CLIENT && ForgeConfigHandler.client.enableSplatterParticles) {
                    ClientHelper.splatter(
                            message.splatterType,
                            message.splatterPosition,
                            message.splatterDirection,
                            message.splatterDamage
                    );
                }
                return null;
            }
        }

    }
}
