package com.okay.reader.plugin.pdf.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by ZhanTao on 7/3/17.
 */

@Entity
public class PdfProgress {
    @Id(autoincrement = true)
    private Long id;

    @Unique private String path;

    private Integer index;
    private Integer sum;
    private Long time;
    private Integer seen;//origin office file has been opened. 0-not; 1-yes

    @Generated(hash = 2056185574)
    public PdfProgress(Long id, String path, Integer index, Integer sum, Long time, Integer seen) {
        this.id = id;
        this.path = path;
        this.index = index;
        this.sum = sum;
        this.time = time;
        this.seen = seen;
    }

    @Generated(hash = 63176235)
    public PdfProgress() {
    }

    @Override
    public String toString() {
        return "PdfProgress{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", index='" + index + '\'' +
                ", sum='" + sum + '\'' +
                ", time='" + time + '\'' +
                ", seen='" + seen + '\'' +
                '}';
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeen() {
        return seen;
    }

    public void setSeen(Integer seen) {
        this.seen = seen;
    }
}
