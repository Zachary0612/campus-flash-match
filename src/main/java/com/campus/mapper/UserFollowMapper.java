package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注关系Mapper接口
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 统计用户关注数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Integer countFollowing(@Param("userId") Long userId);
    
    /**
     * 统计用户粉丝数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Integer countFollowers(@Param("userId") Long userId);
    
    /**
     * 检查是否已关注
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    Integer checkFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
