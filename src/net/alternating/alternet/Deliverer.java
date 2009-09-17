package net.alternating.alternet;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

public interface Deliverer {
    public void throwReceiveEventString(ByteBuffer data) throws CharacterCodingException;
    public void throwReceiveEventByteArray(ByteBuffer data);
}
