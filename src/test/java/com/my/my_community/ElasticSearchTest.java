package com.my.my_community;

import com.my.my_community.entity.DiscussPost;
import com.my.my_community.mapper.DiscussPostMapper;
import com.my.my_community.mapper.repository.DiscussPostRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = MyCommunityApplication.class)
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * 插入数据
     */
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }
    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100,0));
    }
    @Test
    public void testSearchByRepository() throws IOException {
        //构建搜索条件
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //搜索
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(build, DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, build.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            System.out.println(discussPostSearchHit.getHighlightFields()); //高亮内容
            System.out.println(discussPostSearchHit.getContent()); //原始内容
        }
        //下面是封装将高亮部分成一个page对象，也可以不做，直接discussPostSearchHit.getHighlightFields()获取
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            DiscussPost discussPost = discussPostSearchHit.getContent();
            //discussPostSearchHit.getHighlightFields() //高亮
            if (discussPostSearchHit.getHighlightFields().get("title") != null) {
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if (discussPostSearchHit.getHighlightFields().get("content") != null) {
                discussPost.setContent(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            //System.out.println(discussPostSearchHit.getContent());
            list.add(discussPost);
        }
        PageImpl<DiscussPost> pageInfo = new PageImpl<DiscussPost>(list, build.getPageable(), search.getTotalHits());
        System.out.println(pageInfo.getTotalElements());
        System.out.println(pageInfo.getTotalPages());
        System.out.println(pageInfo.getNumber());
        System.out.println(pageInfo.getSize());
        for (DiscussPost discussPost : pageInfo) {
            System.out.println(discussPost);
        }
    }

}
