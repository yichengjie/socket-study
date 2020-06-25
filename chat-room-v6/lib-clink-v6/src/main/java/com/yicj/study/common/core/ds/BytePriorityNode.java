package com.yicj.study.common.core.ds;

/**
 * 带优先级的节点, 可用于构成链表
 */
public class BytePriorityNode<Item> {
    public byte priority;
    public Item item;
    public BytePriorityNode<Item> next;

    public BytePriorityNode(Item item) {
        this.item = item;
    }

    /**
     * 按优先级追加到当前链表中
     *
     * @param node Node
     */
    public void appendWithPriority(BytePriorityNode<Item> node) {
        if (next == null) {
            next = node;
        } else {
            BytePriorityNode<Item> after = this.next;
            //如果后面小于待插入节点的优先级，则插入到中间
            if (after.priority < node.priority) {
                // 中间位置插入
                this.next = node;
                node.next = after;
            } else {// 如果不小则继续向后
                after.appendWithPriority(node);
            }
        }
    }
}