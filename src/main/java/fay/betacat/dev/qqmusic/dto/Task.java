package fay.betacat.dev.qqmusic.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Task {
    /**
     * 任务id
     */
    String uuid;

    /**
     * 资源路径
     */
    String url;

    /**
     * 资源名称
     */
    String name;


}
