package com.hivenote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hivenote.mapper") // 扫描 Mapper 接口位置
public class HiveNoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(HiveNoteApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  HiveNote 后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}