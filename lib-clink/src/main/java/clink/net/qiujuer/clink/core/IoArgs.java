package clink.net.qiujuer.clink.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 用来提供一些 属性
 * 不能无限制的创建buffer
 */
public class IoArgs {
    private int limit = 256;
    private byte[] byteBuffer = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    //从bytes中读取数据  有一个位移
    public int readFrom(byte[] bytes,int offset){
        // 当前可以容纳的大小 buffer.remaining()
       int size = Math.min(bytes.length-offset,buffer.remaining());
        buffer.put(bytes,offset,size);
        return size;
    }

    //从bytes中写数据  有一个位移
    public int writeTo(byte[] bytes,int offset){
        // 当前可以容纳的大小 buffer.remaining()
        //假如bytes空间里面只有10，如果有256个字节，写入bytes，是不行的
        //
        int size = Math.min(bytes.length-offset,buffer.remaining());
        buffer.get(bytes,offset,size);
        return size;
    }

    /**
     * 从SocketChannel读取数据
     * @return
     * @throws IOException
     */
    public int readFrom(SocketChannel channel) throws IOException{
        startWriting();
        int bytesProduced = 0;
        //是否还有容纳区间
        while(buffer.hasRemaining()){
            int len = channel.read(buffer);
            if(len<0){
                //结束符异常
                throw new EOFException();
            }
            bytesProduced += len;
        }
        finishWriting();

        return bytesProduced;
    }

    /**
     * 写数据到SocketChannel
     * @return
     * @throws IOException
     */
    public int writeTo(SocketChannel channel) throws IOException{
        int bytesProduced = 0;
        //是否还有容纳区间
        while(buffer.hasRemaining()){
            int len = channel.write(buffer);
            if(len<0){
                //结束符异常
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * 开始写入数据到IoArgs
     */
    public void startWriting(){
        buffer.clear();
        //定义容纳区间  每次调用一个clear后，容纳区间在末尾
        buffer.limit(limit);
    }

    /**
     * 写完数据后调用
     */
    public void finishWriting(){
        //反转一下  把写操作变成一个读操作，以便后面可以吧数据从buffer中读出来
      buffer.flip();
    }

    //设置一个limit大小

    /**
     * 设置单次写操作的容纳区间
     * @param limit
     */
    public void limit(int limit){
        this.limit = limit;
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength() {
        /*if(buffer.remaining()>0){*/
            return buffer.getInt();
       /* }
        return 0; */
    }

    public int capacity() {
        //buffer的容量
        return buffer.capacity();
    }


  /*  public String bufferString(){
        //丢弃换行符
        //return new String(byteBuffer,0,buffer.position()-1);
        return new String(byteBuffer,0,buffer.position());
    }*/

    //监听ioArgs的状态
    public interface IoArgsEventListener{
        //开始时，回调
        void onStarted(IoArgs args);
        //结束时，回调
        void onCompleted(IoArgs args);
    }

}
