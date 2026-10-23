package com.zeroone.star.entity;

/**
 * 社团活动实体。
 * 当前只提供活动浏览和管理员发布、删除，不包含成员报名。
 */
public class Activity {

    public long id;
    public long clubId;
    public String clubName;
    public String title;
    public String description;
    public String startTime;
    public String location;
    public String publisherName;
    public String createdAt;
<<<<<<< HEAD
    public long createdBy;
=======
>>>>>>> feature/dao-a-user-query
}
