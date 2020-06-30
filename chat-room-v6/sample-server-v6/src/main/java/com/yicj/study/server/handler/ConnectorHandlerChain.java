package com.yicj.study.server.handler;

/**
 * ClassName: ConnectorHandlerChain
 * Description: TODO(描述)
 * Date: 2020/6/30 22:13
 *
 * @author yicj(626659321 @ qq.com)
 * 修改记录
 * @version 产品版本信息 yyyy-mm-dd 姓名(邮箱) 修改信息
 */
public abstract class ConnectorHandlerChain <Model>{

    private volatile ConnectorHandlerChain<Model> next ;


    public ConnectorHandlerChain<Model> appendLast(ConnectorHandlerChain<Model> newChain){
        if (newChain == this || this.getClass().equals(newChain.getClass())){
            return this ;
        }
        synchronized (this){
            if (next ==null){
                next = newChain ;
                return newChain ;
            }
            return next.appendLast(newChain) ;
        }
    }

    public synchronized boolean handle(ClientHandler handler, Model model) {
        // 注意这样第一步就要这样赋值，以防止在consume对next重新追加等操作
        ConnectorHandlerChain<Model> next = this.next ;
        if (consume(handler, model)){
            return true ;
        }
        boolean consumed = next != null && next.handle(handler, model) ;
        if (consumed){
            return true ;
        }
        return consumeAgain(handler,model) ;
    }

    protected abstract boolean consume(ClientHandler handler, Model model) ;

    protected boolean consumeAgain(ClientHandler handler, Model model){
        return false ;
    }

}
