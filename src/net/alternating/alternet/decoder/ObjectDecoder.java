package net.alternating.alternet.decoder;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import net.alternating.alternet.Deliverer;
import net.alternating.alternet.ObjectClient;
import net.alternating.alternet.Utils;

public class ObjectDecoder {
    
    protected static final int STATE_SIZE = 0;
    protected static final int STATE_DATA = 1;
    
    //protected static final byte TYPE_STRING = 0;
    //protected static final byte TYPE_JPG = 1;
    //protected static final byte TYPE_PNG = 2;
    
    private int state;
    private int read;
    private int size;
    
    protected ByteBuffer data;
    private Deliverer deliverer;
    
    
    
    public ObjectDecoder(Deliverer d) {
        state = ObjectDecoder.STATE_SIZE;
        read = 0;
        size = 0;
        data = ByteBuffer.allocate(2000);
        this.deliverer = d;
    }
    
    public void decode(ByteBuffer bf, int length) throws CharacterCodingException {
        switch(state){
        case STATE_SIZE:
            for(int i = 0; i < length && data.hasRemaining(); i++)
                data.put(bf.get());
            
            if(data.position() >= 4) {
                state = STATE_DATA;
                //FIXME use getInt() instead
                byte[] sizeBytes = new byte[4];
                data.rewind();
                data.get(sizeBytes);
                size = Utils.byteArrayToInt(sizeBytes);
                if(size == 0) {
                	state = STATE_SIZE;
                	data.rewind();
                	return;
                }
                ByteBuffer newData = ByteBuffer.allocate(size);
                ByteBuffer oldData = data;
                data = newData;
                
                
                if(length - 4 > 0) {
                	int newLength = Math.min(length - 4,oldData.capacity() - 4);

                	decode(oldData, oldData.capacity()-4);
                }
                if(length > oldData.capacity())
                    decode(bf,length - oldData.capacity());
            }
            else {
                read += length;
            }
            break;
        case STATE_DATA:
        	int copied = 0;
            for(int i = 0; i < size && data.hasRemaining() && bf.hasRemaining(); i++) {
                data.put(bf.get());
                copied++;
            }
            read += copied;
            if(read == size) {
                deliverer.throwReceiveEventString(data);
                deliverer.throwReceiveEventByteArray(data);
                read = 0;
                state = STATE_SIZE;
                data = ByteBuffer.allocate(20000);
                if(size < length) {
                    decode(bf, length - size);
                }
            }
            break;
        }
    }
}
