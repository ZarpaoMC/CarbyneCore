package com.medievallords.carbyne.utils.signgui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class WrapperPlayServerUpdateSign extends AbstractPacket {

    public static final PacketType TYPE;
    
    static {
        TYPE = PacketType.Play.Server.UPDATE_SIGN;
    }
    
    public WrapperPlayServerUpdateSign() {
        super(new PacketContainer(WrapperPlayServerUpdateSign.TYPE), WrapperPlayServerUpdateSign.TYPE);
        this.handle.getModifier().writeDefaults();
    }
    
    public WrapperPlayServerUpdateSign(final PacketContainer packet) {
        super(packet, WrapperPlayServerUpdateSign.TYPE);
    }
    
    public BlockPosition getLocation() {
        return this.handle.getBlockPositionModifier().read(0);
    }
    
    public void setLocation(final BlockPosition value) {
        this.handle.getBlockPositionModifier().write(0, value);
    }
    
    public WrappedChatComponent[] getLines() {
        return this.handle.getChatComponentArrays().read(0);
    }
    
    public void setLines(final WrappedChatComponent[] value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null!");
        }

        if (value.length != 4) {
            throw new IllegalArgumentException("value must have 4 elements!");
        }

        this.handle.getChatComponentArrays().write(0, value);
    }
}
