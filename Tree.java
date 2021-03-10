package com.lcj.leetcode.summary;


import com.atlassian.guava.base.Strings;
import com.atlassian.guava.collect.Lists;

import java.util.*;

/**
 * @author wangguowei
 * @date 2020/11/12 19:46
 */
public class Tree  {

    private TreeNode root;

    /**
     * 数组转化为二叉树(需要先生成Node节点，再创建节点间的关系)
     * 由于数组下标从0开始，用数组模拟二叉树（当然也包括堆）的话，如果根节点的下标为0的话，
     * 则对于每个结点i，其左孩子下标为2*i+1；其右孩子下标为2*i+2
     *  数组中没有存值时表示节点为空，前n/2个节点是父节点
     * @return
     */
    public  TreeNode  buildTreeByPreOrder(List<String> list) {
        if(list == null || list.size() == 0) {
            return null;
        }
        List<TreeNode> nodeList = Lists.newArrayList();  // 需要先生成所有的节点
        for(int i=0;i<list.size();i++) {
            if(Strings.isNullOrEmpty(list.get(i))) {
                nodeList.add(null);
            } else {
                nodeList.add(new TreeNode(list.get(i)));
            }
        }
        for(int i=0;i<list.size()/2;i++) {
            if(Strings.isNullOrEmpty(list.get(i))) {
                continue;
            }
            TreeNode treeNode = nodeList.get(i);
            if(!Strings.isNullOrEmpty(list.get(i*2+1))) {
                treeNode.setLeft(nodeList.get(i*2+1));
            }
            if(!Strings.isNullOrEmpty(list.get(i*2+2))) {
                treeNode.setRight(nodeList.get(i*2+2));
            }
        }
        return nodeList.get(0);
    }

    public void preOrderList(List<TreeNode> preStore,List<TreeNode> inStore,List<TreeNode> backStore,TreeNode root) {
        if(root != null) {
            preStore.add(root);
            preOrderList(preStore,inStore,backStore,root.getLeft());
            inStore.add(root);
            preOrderList(preStore,inStore,backStore,root.getRight());
            backStore.add(root);
        }
    }

    // 前序非递归遍历
    public void preOrderWithNoRecurse(TreeNode root) {
        List<TreeNode> list = Lists.newArrayList();
        Stack<TreeNode> stack = new Stack<>();
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                System.out.println(root.getValue());
                stack.push(root);
                root = root.getLeft();
            }
            TreeNode node = stack.pop();
            root = node.getRight();
        }
    }

    // 中序非递归遍历
    public void inOrderWithNoRecurse(TreeNode root) {
        List<TreeNode> list = Lists.newArrayList();
        Stack<TreeNode> stack = new Stack<>();
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                stack.push(root);
                root = root.getLeft();
            }
            TreeNode node = stack.pop();
            System.out.println(node.getValue());
            root = node.getRight();
        }
    }

    // 后非递归遍历
    public void postOrderWithNoRecurse(TreeNode root) {
        java.util.LinkedList<TreeNode> list = new java.util.LinkedList<>();
        Stack<TreeNode> stack = new Stack<>();
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                list.addFirst(root);
                stack.push(root);
                root = root.getRight();
            }
            TreeNode node = stack.pop();
            root = node.getLeft();
        }
        list.stream().forEach(one->System.out.println(one.getValue()));
    }

    // 按层遍历  使用queue  BFS
    public void levalOrderWithNoRecurse(TreeNode root) {
        java.util.LinkedList<TreeNode> queue = new java.util.LinkedList<>();
        queue.push(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for(int i=0;i<size;i++) {
                TreeNode node = queue.removeLast();
                if(node.getLeft() != null) {
                    queue.push(node.getLeft());
                }
                if(node.getRight() != null) {
                    queue.push(node.getRight());
                }
                System.out.print(node.getValue()+" ");
            }
            System.out.println();
        }
    }

    /**
     * 根据前序与中序序列创建二叉树
     * @param preOrder
     * @param inOrder
     * @return
     */
    public TreeNode buildTreeWithPreOrderAndInOrder(List<String> preOrder, List<String> inOrder) {
        if(preOrder == null || inOrder == null || preOrder.size() == 0 || inOrder.size() == 0) {
            return null;
        }
        TreeNode root = buildTree_one(preOrder, 0, preOrder.size()-1,inOrder,0, inOrder.size()-1);
        return root;
    }

    private TreeNode buildTree_one(List<String> preOrder, int preStart, int preEnd, List<String> inOrder, int inStart,int inEnd) {
        if(preStart == preEnd) {
            TreeNode node = new TreeNode(preOrder.get(preStart));
            return node;
        }
        if(inStart == inEnd) {
            TreeNode node = new TreeNode(inOrder.get(inStart));
            return node;
        }

        TreeNode root = new TreeNode(preOrder.get(preStart));   // 创建root节点同时找到root节点在中序中的位置
        int index = -1;
        for(int i=inStart;i<=inEnd;i++) {
            if(inOrder.get(i).equals(preOrder.get(preStart))) {
                index = i;
            }
        }

        // 递归创建左右子树
        TreeNode left = buildTree_one(preOrder, preStart+1, preStart+index-inStart, inOrder, inStart, index-1);
        TreeNode right = buildTree_one(preOrder,preStart+index-inStart+1,preEnd,inOrder,index+1,inEnd);
        root.setLeft(left);
        root.setRight(right);
        return root;
    }

    /**
     * 根据后序与中序序列创建二叉树
     * @param postOrder
     * @param inOrder
     * @return
     */
    public TreeNode buildTreeWithPostOrderAndInOrder(List<String> postOrder, List<String> inOrder) {
        if(postOrder == null || inOrder == null || postOrder.size() == 0 || inOrder.size() == 0) {
            return null;
        }
        TreeNode root = buildTree_two(postOrder, 0, postOrder.size()-1,inOrder,0, inOrder.size()-1);
        return root;
    }

    private TreeNode buildTree_two(List<String> postOrder, int postStart, int postEnd, List<String> inOrder, int inStart, int inEnd) {
        if(postStart >= postEnd) {
            return new TreeNode(postOrder.get(postEnd));
        }
        if(inStart >= inEnd) {
            return new TreeNode(inOrder.get(inEnd));
        }

        TreeNode root = new TreeNode(postOrder.get(postEnd));  // 创建root节点同时找到root节点在中序中的位置
        int index = -1;
        for(int i=inStart;i<=inEnd;i++) {
            if(inOrder.get(i).equals(postOrder.get(postEnd))) {
                index = i;
            }
        }

        TreeNode right = buildTree_two(postOrder,postEnd-(inEnd-index),postEnd-1,inOrder,index+1,inEnd);
        TreeNode left = buildTree_two(postOrder, postStart, postStart+(index-inStart-1),inOrder,inStart, index-1);
        root.setLeft(left);
        root.setRight(right);
        return root;
    }

    public void preSerializable (TreeNode root,StringBuilder stringBuilder) {
        if(root == null) {
            stringBuilder.append("#");
            return;
        }
        stringBuilder.append(root.getValue());
        preSerializable(root.getLeft(),stringBuilder);
        preSerializable(root.getRight(),stringBuilder);
    }

    public TreeNode preReverseSerializable (java.util.LinkedList<String> list) {
        if(list == null || list.size() == 0) {
            return null;
        }
        String str = list.removeFirst();
        if(str.equals("#")) {
            return null;
        }
        TreeNode root = new TreeNode(str);
        root.setLeft(preReverseSerializable(list));
        root.setRight(preReverseSerializable(list));
        return root;
    }

    Map<TreeNode, TreeNode> parentMap = new HashMap<>();
    /**
     *  寻找所有节点的子节点
     */
    public void findParentNode(TreeNode root) {
        if(root == null) {
            return;
        }
        TreeNode left = root.getLeft();
        if(left != null) {
            parentMap.put(root.getLeft(), root);
        }
        TreeNode right = root.getRight();
        if(right != null) {
            parentMap.put(root.getRight(), root);
        }
        findParentNode(left);
        findParentNode(right);
    }

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null || root == p || root == q) {
            return root;
        }
        java.util.LinkedList<TreeNode> list = new java.util.LinkedList<>();
        findParentNode(root);
        while (p != null) {
            list.addLast(p);
            p = parentMap.get(p);
        }
        while (q != null) {
            if(list.contains(q)) {
                return q;
            }
            q = parentMap.get(q);
        }
        return null;
    }

    int maxResult = Integer.MIN_VALUE;

    // 二叉树以root为根节点的某一侧（左子树/右子树）最大和
    public int getMaxSum(TreeNode root) {
        if(root == null) {
            return 0;
        }
        int left = Math.max(getMaxSum(root.getLeft()), 0);
        int right = Math.max(getMaxSum(root.getRight()), 0);
        maxResult = Math.max(maxResult, Integer.parseInt(root.getValue()) + left + right);
        return Integer.parseInt(root.getValue()) + Math.max(left, right);
    }

    public boolean isAVL(TreeNode root) {
        if(root == null) {
            return true;
        }
        int left = high(root.getLeft());
        int right = high(root.getRight());
        return Math.abs(left-right)<=1 && isAVL(root.getRight()) && isAVL(root.getLeft());
    }

    public int high(TreeNode node) {
        if(node == null) {
            return 0;
        }
        return Math.max(high(node.getLeft()), high(node.getRight())) + 1;
    }

    public boolean isSearchTree(TreeNode root, Integer lower, Integer higher) {
        if(root == null) {
            return true;
        }
        if(lower != null && Integer.parseInt(root.getValue()) < lower.intValue()) {
            return false;
        }
        if(higher != null && Integer.parseInt(root.getValue()) > higher.intValue()) {
            return false;
        }
        return isSearchTree(root.getLeft(), lower, Integer.parseInt(root.getValue())) && isSearchTree(root.getRight(),  Integer.parseInt(root.getValue()), higher);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int s = scanner.nextInt();
        System.out.println(s);
        Tree tree = new Tree();
        TreeNode root = tree.buildTreeByPreOrder(Lists.newArrayList("-1","0","3","-2","4","","","8",""));
        TreeNode node =   tree.lowestCommonAncestor(root, root.getLeft().getLeft().getLeft(), root.getLeft());
        System.out.println(tree.parentMap);
        List<TreeNode> preStore = Lists.newArrayList();
        List<TreeNode> inStore = Lists.newArrayList();
        List<TreeNode> backStore = Lists.newArrayList();
        tree.preOrderList(preStore,inStore,backStore,root);
//        tree.preOrderWithNoRecurse(root);
//        tree.inOrderWithNoRecurse(root);
//        tree.postOrderWithNoRecurse(root);
//        StringBuilder stringBuilder = new StringBuilder();
//        tree.preSerializable(root,stringBuilder);
//        LinkedList<String> linkedList = new LinkedList<>(Arrays.asList(stringBuilder.toString().split("")));
//        tree.levalOrderWithNoRecurse(root);
        List<String> inOrder = Lists.newArrayList("c","b","d","a","e");
        List<String> postOrder = Lists.newArrayList("c","d","b","e","a");
        TreeNode treeNode = tree.buildTreeWithPostOrderAndInOrder(postOrder,inOrder);
    }
}
