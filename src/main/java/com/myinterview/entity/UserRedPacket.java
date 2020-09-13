package com.myinterview.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserRedPacket implements Serializable {
    //实体类要继承接口Serializable这样才可以被序列化保存
    private static final long serialVersionUID = -7341118874172649480L;
    private Long id;//编号
    private Long redPacketId;//红包编号
    private Long userId;//抢用户编号
    private Double amount;//抢红包金额
    private Timestamp grabTime;//抢红包时间
    private String note;//备注

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(Long redPacketId) {
        this.redPacketId = redPacketId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Timestamp getGrabTime() {
        return grabTime;
    }

    public void setGrabTime(Timestamp grabTime) {
        this.grabTime = grabTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
