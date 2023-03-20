package com.my.my_community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
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
    //替换符
    private static final String REPLACEMENT = "***";
    //根节点
    TrieNode root = new TrieNode();

    /**初始化前缀树
     *
     */
    //@PostConstruct：当容器实例化这个bean，构造之后，这个方法就会被调用
    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                //添加敏感词到前缀树
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败："+e.getMessage());
        }


    }

    //把一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode = root;//当前节点
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                //如果不存在改节点，添加节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一层循环
            tempNode = subNode;
            //循环结束，在最后一个字符设上结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        //判断是否为空
        if(StringUtils.isBlank(text)){
            return null;
        }
        //三个指针
        TrieNode tempNode = root;
        int begin = 0;
        int position = 0;
        //记录最终返回结果
        StringBuilder sb = new StringBuilder();
        //以指针为循环
        while(begin < text.length()){
            if(position < text.length()){
                Character c = text.charAt(position);//当前节点
                //要跳过特殊符号（比如吸⭐毒，就得把这个跳过）
                if(isSymbol(c)){
                    // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                    if(tempNode == root){
                        sb.append(c);
                        begin++;
                    }
                    //不然直接向前
                    position++;
                    continue;
                }
                //检查下级节点
                tempNode = tempNode.getSubNode(c);
                if(tempNode == null){//下级没有节点，这说明以begin开头的字符串不是敏感词
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = root;
                }// 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                }
                // 检查下一个字符
                else {
                    position++;
                }

            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }
        }
        return sb.toString();
    }
    //0x2E80~0x9FFF是东亚文字范围
    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
    private class TrieNode{
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
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
