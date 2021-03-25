package com.wenlei.community.dao.elasticsearch;

import com.wenlei.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository <DiscussPost,Integer>{
}
