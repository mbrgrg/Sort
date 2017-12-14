package test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//sta classe lasciala stare che mi serve solo per fare dei test
public class Test {
    static public void main( String args[] ) throws Exception
    {
        File fl = new File("file.txt");
        FileInputStream fileInputStream = new FileInputStream(fl);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)fl.length());
        long [] list = new long [(int)fl.length()];
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        int limit = byteBuffer.limit();
        String r="";
        int i=0;
        while(limit>0)
        {
            char c = (char)byteBuffer.get();
            if(Character.isDigit(c))
                r=r+c;
            else if(c == '\n'){
                list[i] = Long.valueOf(r);
                i++;
                r="";
            }
            limit--;
        }
        for(i=0;i<list.length;i++)
            System.out.println(list[i]);
        fileChannel.close();
    }
}