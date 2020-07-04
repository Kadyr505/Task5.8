package ru.vsu.cs.course1.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Реализация простейшего бинарного дерева
 */
public class BinaryTree<T> implements DefaultBinaryTree<T> {

    public static List<Double> list = new ArrayList<Double>();

    protected static class SimpleTreeNode<T> implements DefaultBinaryTree.TreeNode<T> {
        public T value;
        public SimpleTreeNode<T> left;
        public SimpleTreeNode<T> right;


        public SimpleTreeNode(T value, SimpleTreeNode<T> left, SimpleTreeNode<T> right) {
            this.value = value;
            this.left = left;
            this.right = right;

                double min = (int) value;
                if (left != null) {
                    if (min > min(left)) {
                        min = min(left);
                    }
                }
                list.add(min);
        }

        public SimpleTreeNode(T value)  {
            this(value, null, null);

        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public SimpleTreeNode<T> getLeft() {
            return left;
        }

        @Override
        public SimpleTreeNode<T> getRight() {
            return right;
        }


        public double min(SimpleTreeNode node) {
            double min = (int)node.value;
            if (node.left != null) {
                if (min > min(node.left)){
                    min = min(node.left);
                }
            }
            list.add(min);
            return min;
        }
    }
    
    protected SimpleTreeNode<T> root = null;
    
    protected Function<String, T> fromStrFunc;
    protected Function<T, String> toStrFunc;
    
    public BinaryTree(Function<String, T> fromStrFunc, Function<T, String> toStrFunc) {
        this.fromStrFunc = fromStrFunc;
        this.toStrFunc = toStrFunc;
    }

    public BinaryTree(Function<String, T> fromStrFunc) {
        this(fromStrFunc, x -> x.toString());
    }
    
    public BinaryTree() {
        this(null);
    }
    
    @Override
    public SimpleTreeNode<T> getRoot() {
        return root;
    }
    
    public void clear() {
        root = null;
    }
    
    private T fromStr(String s) throws Exception {
        s = s.trim();
        if (s.length() > 0 && s.charAt(0) == '"') {
            s = s.substring(1);
        }
        if (s.length() > 0 && s.charAt(s.length() - 1) == '"') {
            s = s.substring(0, s.length() - 1);
        }
        if (fromStrFunc == null) {
            throw new Exception("Не определена функция конвертации строки в T");
        }
        return fromStrFunc.apply(s);
    }
    
    private class IndexWrapper {
        public int index = 0;
    }
    
    private void skipSpaces(String bracketStr, IndexWrapper iw) {
        while (iw.index < bracketStr.length() && Character.isWhitespace(bracketStr.charAt(iw.index))) {
            iw.index++;
        }
    }
    
    private T readValue(String bracketStr, IndexWrapper iw) throws Exception {
        // пропуcкаем возможные пробелы
        skipSpaces(bracketStr, iw);
        if (iw.index >= bracketStr.length()) {
            return null;
        }
        int from = iw.index;
        boolean quote = bracketStr.charAt(iw.index) == '"';
        if (quote) {
            iw.index++;
        }
        while (iw.index < bracketStr.length() && (
                    quote && bracketStr.charAt(iw.index) != '"' ||
                    !quote && !Character.isWhitespace(bracketStr.charAt(iw.index)) && "(),".indexOf(bracketStr.charAt(iw.index)) < 0
               )) {
            iw.index++;
        }
        if (quote && bracketStr.charAt(iw.index) == '"') {
            iw.index++;
        }
        String valueStr = bracketStr.substring(from, iw.index);
        T value = fromStr(valueStr);
        skipSpaces(bracketStr, iw);
        return value;
    }
    
    private SimpleTreeNode<T> fromBracketStr(String bracketStr, IndexWrapper iw) throws Exception {
        T parentValue = readValue(bracketStr, iw);
        SimpleTreeNode<T> parentNode = new SimpleTreeNode<T>(parentValue);
        if (bracketStr.charAt(iw.index) == '(') {
            iw.index++;
            skipSpaces(bracketStr, iw);
            if (bracketStr.charAt(iw.index) != ',') {
                SimpleTreeNode<T> leftNode = fromBracketStr(bracketStr, iw);
                parentNode.left = leftNode;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) == ',') {
                iw.index++;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                SimpleTreeNode<T> rightNode = fromBracketStr(bracketStr, iw);
                parentNode.right = rightNode;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                throw new Exception(String.format("Ожидалось ')' [%d]", iw.index));
            }
            iw.index++;
        }
        
        return parentNode;
    }
    
    public void fromBracketNotation(String bracketStr) throws Exception {
        IndexWrapper iw = new IndexWrapper();
        SimpleTreeNode<T> root = fromBracketStr(bracketStr, iw);
        if (iw.index < bracketStr.length()) {
            throw new Exception(String.format("Ожидался конец строки [%d]", iw.index));
        }
        this.root = root;
    }
}
