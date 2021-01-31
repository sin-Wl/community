package com.wenlei.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // @PostConstruct表示当前方法是一个初始化方法，当容器实例化这个Bean以后，当调用构造器后，这个方法就会被自动调用（只初始化一次）
    @PostConstruct
    public void init() {
        try (
                // 类加载器会从类路径（classes目录）下加载资源，getResourceAsStream("文件名")可以加载到文件的字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    // 将一个敏感词添加到前缀树当中去
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); ++i) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            // 如果没有这个关键词，则初始化子节点
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            // 设置结束的标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }

        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1，指向前缀树
        TrieNode tempNode = rootNode;
        // 指针2，指向字符串的首位
        int begin = 0;
        // 指针3，指向子字符串的末尾
        int position = 0;

        // 返回的结果
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {
            if (position < text.length()) {
                char c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    // 若指针1处于根节点，说明关键词一个字也没匹配，则需要让指针2向下走一步，指针3向下走一步
                    if (tempNode == rootNode) {
                        sb.append(c);
                        ++begin;
                    }
                    // 无论符号在关键词匹配的开头或者中间，也就是说不论指针2是否向下走，指针3都必须向下走
                    ++position;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);

                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 指针1重新指向根节点
                    tempNode = rootNode;
                } else if (tempNode.isKeywordEnd()) {
                    // 发现敏感词，将begin~position之间的字符串替换掉
                    sb.append(REPLACEMENT);
                    // 进入下一个位置
                    begin = ++position;
                    // 指针1重新指向根节点
                    tempNode = rootNode;
                } else {
                    // 处于匹配过程中，则position继续向下走
                    ++position;
                }
            } else {
                // position已经走到结尾，但是begin还未到结尾，说明这段字符串未成功匹配到敏感词
                // 则将bengin位置上的字符加入结果中，然后指针继续向下走
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // isAsciiAlphanumeric方法判断字符是否是一个普通（合法）字符，0x2E80~0x9FFF 是东亚文字范围，包括中文，日文，韩文等等
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean keywordEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return keywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            this.keywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }


    }


}