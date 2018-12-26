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
    String index;

    /**
     * 发行时间
     */
    String publicTime;

    /**
     * 歌曲品质(128、320、aac、ape、flac)
     */
    List<String> type;


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
