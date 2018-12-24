package fay.betacat.dev.qqmusic.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Album {

    String mid;

    /**
     * 专辑名称
     */
    String name;

    /**
     * 专辑图片
     */
    String pic;
}
