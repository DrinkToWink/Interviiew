package com.myinterview.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class RedPacket implements Serializable {
    //实体类要继承接口Serializable这样才可以被序列化保存
    private static final long serialVersionUID = 892464151374404842L;
    private Long id;//红包编号
    private Long userId;//发红包用户
    private Double amount;//红包金额
    private Timestamp sendDate;//发红包时间
    private Integer total;//小红包总数
    private Double unitAmount;//单个红包金额
    private Integer stock;//剩余红包个数
    private Integer version;//版本
    private String note;//备注

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Timestamp getSendDate() {
        return sendDate;
    }

    public void setSendDate(Timestamp sendDate) {
        this.sendDate = sendDate;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Double getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(Double unitAmount) {
        this.unitAmount = unitAmount;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
