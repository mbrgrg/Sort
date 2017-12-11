package sort;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//sta classe lasciala stare che mi serve solo per fare dei test
public class Test {
    static public void main( String args[] ) throws Exception 
    {
        FileInputStream fileInputStream = new FileInputStream(
                                        new File("eee.txt"));
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        int limit = byteBuffer.limit();
        while(limit>0)
        {
            System.out.print((char)byteBuffer.get());
            limit--;
        }

        fileChannel.close();
    }
}
