package com.yicj.study.common.impl.async;

import com.yicj.study.common.core.Frame;
import com.yicj.study.common.core.IoArgs;
import com.yicj.study.common.core.SendPacket;
import com.yicj.study.common.core.ds.BytePriorityNode;
import com.yicj.study.common.frames.AbsSendPacketFrame;
import com.yicj.study.common.frames.CancelSendFrame;
import com.yicj.study.common.frames.SendEntityFrame;
import com.yicj.study.common.frames.SendHeaderFrame;

import java.io.Closeable;
import java.io.IOException;

/**
 * 用户管理具体package的发送与接收
 * ClassName: AsyncPacketReader
 * Description: TODO(描述)
 * Date: 2020/6/25 20:05
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public class AsyncPacketReader implements Closeable {
    private PacketProvider provider ;
    private volatile IoArgs args = new IoArgs() ;
    private volatile BytePriorityNode<Frame> node ;
    private volatile int nodeSize = 0 ;

    // 1,2,3,4...255
    private short lastIdentifier = 0 ;


    AsyncPacketReader(PacketProvider provider){
        this.provider = provider ;
    }


    /**
     * 请求从队列中拿一份packet进行发送
     * @return 如果当前Reader中有可以用于网络发送的数据，则返回true
     */
    boolean requestTaskPacket() {
        synchronized (this){
            if (nodeSize >= 1){
                return true ;
            }
        }
        SendPacket packet = provider.takePacket();
        if (packet !=null){
            short identifier = generateIdentifier() ;
            SendHeaderFrame frame = new SendHeaderFrame(identifier, packet) ;
            appendNewFrame(frame) ;
        }
        synchronized (this){
            return nodeSize != 0 ;
        }
    }

    IoArgs fillDate() {
        Frame currentFrame = gerCurrentFrame();
        if (currentFrame ==null){
            return null ;
        }
        try {
            if (currentFrame.handle(args)){
                //消费完本帧
                // 尝试基于本帧构建后续帧
                Frame nextFrame = currentFrame.nextFrame();
                if (nextFrame !=null){
                    appendNewFrame(nextFrame);
                }else if (currentFrame instanceof SendEntityFrame){
                    //末尾实体帧,通知完成
                    provider.completedPacket(((SendEntityFrame) currentFrame).getPacket(),true);
                }
                // 从链表头弹出
                popCurrentFrame() ;
            }
            return args ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null ;
    }


    synchronized void  cancel(SendPacket packet) {
        if (nodeSize ==0){
            return;
        }
        for (BytePriorityNode<Frame> x= node, before =null ; x!=null; before= x, x= x.next){
             Frame frame = x.item ;
             if (frame instanceof AbsSendPacketFrame){
                 AbsSendPacketFrame packetFrame = (AbsSendPacketFrame) frame ;
                 if (packetFrame.getPacket() == packet){
                     boolean removable = packetFrame.abort();
                     if (removable){
                         removeFrame(x, before) ;
                         if (packetFrame instanceof SendHeaderFrame){
                             // 头帧，并且未被发送任何数据，直接取消后不需要添加取消发送帧
                             break;
                         }
                     }
                     // 添加终止帧，通知接收方
                     CancelSendFrame cancelSendFrame = new CancelSendFrame(packetFrame.getBodyIdentifier()) ;
                     appendNewFrame(cancelSendFrame);
                     // 意外终止，返回失败
                     provider.completedPacket(packet,false);
                     break;
                 }
             }
        }

    }



    @Override
    public synchronized void close() {
        while (node !=null){
            Frame frame = node.item ;
            if (frame instanceof AbsSendPacketFrame){
                SendPacket packet = ((AbsSendPacketFrame) frame).getPacket() ;
                provider.completedPacket(packet,false);
            }
            node = node.next ;
        }
        nodeSize = 0 ;
        node = null ;
    }

    private synchronized void appendNewFrame(Frame frame) {
        BytePriorityNode<Frame> newNode = new BytePriorityNode<>(frame) ;
        if (node != null){
            // 使用优先级别添加到链表
            node.appendWithPriority(newNode);
        }else {
            node = newNode ;
        }
        nodeSize ++ ;
    }

    //拿到当前帧
    private synchronized Frame gerCurrentFrame() {
        if (node ==null){
            return null ;
        }
        return node.item ;
    }

    private synchronized void popCurrentFrame() {
        node = node.next ;
        nodeSize -- ;
        if (node ==null){
            requestTaskPacket() ;
        }
    }

    private synchronized void removeFrame(BytePriorityNode<Frame> removeNode, BytePriorityNode<Frame> before) {
        if (before == null){
            // A B C
            // B C
            node = removeNode.next ;
        }else {
            // A B C
            // A C
            before.next  = removeNode.next ;
        }
        nodeSize -- ;
        if (node == null){
            requestTaskPacket() ;
        }
    }

    private short generateIdentifier(){
        short identifier = ++ lastIdentifier ;
        if (identifier == 255){
            lastIdentifier = 0 ;
        }
        return identifier ;
    }

    interface PacketProvider{
        SendPacket takePacket() ;

        void completedPacket(SendPacket packet, boolean isSuccess) ;
    }
}
