package fay.betacat.dev.qqmusic.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Song {

    private String mid;

    /**
     * 歌曲名
     */
    String name;

    /**
     * 专辑中的曲目
     */
    int index;

    /**
     * 发行时间
     */
    String publicTime;

    /**
     * 歌曲品质(128、320、aac、ape、flac)
     */
    String type_128;
    String type_320;
    String type_aac;
    String type_ape;
    String type_flac;


    /**
     * 歌手名
     */
    String singerName;


    String albummid;

    /**
     * 专辑名称
     */
    String albumName;

    /**
     * 专辑图片
     */
    String pic;
}
