package com.lcj.leetcode.summary;

/**
 * @author wangguowei
 * @date 2021/2/8 15:47
 */
public class LinkedList {

     static class Node {
         private int val;
         private Node next;
         public Node(int val) {
             this.val = val;
         }
     }

     public static Node reverseList(Node head) {
         if(head == null || head.next == null) {
             return head;
         }
         Node pre = null;
         Node current = head;
         while (current != null) {
             Node next = current.next;
             current.next = pre;
             pre = current;
             current = next;
         }
         return pre;
     }

     public static Node reverseListPre(Node head, int n) {  // 翻转前n个节点
          Node temp = head;
         while (n > 1) {
            temp = temp.next;
            n--;
         }
         Node next = temp.next;
         temp.next = null;
         Node newHead = reverseList(head);
         head.next = next;
         return newHead;
     }

     // 对链表进行归并排序
     public static Node sortList(Node head) {
         if(head == null || head.next == null) {
             return head;
         }
         // 找到链表的中间节点，从中间节点分隔为两个链表
         Node slow = head;
         Node fast = head.next;
         while (fast != null && fast.next != null) {
             slow = slow.next;
             fast = fast.next.next;
         }
         Node temp = slow.next;
         slow.next = null;

         // 对分隔的链表进行归并
         Node AHead = sortList(head);
         Node BHead = sortList(temp);

         // 合并排序后的两个子链表
         Node newHead = new Node(-1);
         Node tt = newHead;
         while (AHead != null && BHead != null) {
             if(AHead.val < BHead.val) {
                 tt.next = AHead;
                 AHead = AHead.next;
             } else {
                 tt.next = BHead;
                 BHead = BHead.next;
             }
             tt = tt.next;
         }
         tt.next = AHead == null? BHead : AHead;
         return newHead.next;
     }

     // 寻找两个链表的相交节点
     public static Node findIntersect(Node headA, Node headB) {
        if(headA == null || headB == null) {
            return null;
        }
        Node tempA = headA;
        Node tempB = headB;
        while (tempA != tempB) {
            tempA = tempA == null ? headB : tempA.next;
            tempB = tempB == null ? headA : tempB.next;
        }
        return tempA;
     }

     // 寻找环形链表的入口节点
     public static Node loopList (Node head) {
        if(head == null || head.next == head) {
            return head;
        }
        Node slow = head;
        Node fast = head;
        while (fast != null) {
            slow = slow.next;
            if(fast.next == null) {
                return null;
            }
            fast = fast.next.next;
            if(fast == slow) {
                Node temp = head;
                while (temp != slow) {
                    temp = temp.next;
                    slow = slow.next;
                }
                return temp;
            }
        }
        return null;
     }

    public static void main(String[] args) {
        Node head = new Node(0);
        head.next = new Node(1);
        head.next.next = new Node(2);
        head.next.next.next = new Node(3);
     //   Node node = reverseList(head);
        Node node2 = reverseListPre(head, 2);
    }
}
