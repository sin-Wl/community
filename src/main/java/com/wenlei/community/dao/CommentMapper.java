package com.wenlei.community.dao;

import com.wenlei.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCommentByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    int selectPostCommentCountByUserId(int userId, int entityType);

    Comment selectCommentById(int id);
/*

    // 更新指定评论的状态
    int updateStatus(int id, int status);

    // 更新指定帖子的相关评论的状态
    int updateStatusByPostId(int postId,int status);*/

}
