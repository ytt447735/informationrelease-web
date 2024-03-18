package com.gccloud;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.gccloud.common.constant.CommonConst;
import com.gccloud.dataroom.core.constant.DataRoomConst;
import com.gccloud.dataset.constant.DatasetConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
/**
 * @author hongyang
 * @version 1.0
 * @date 2023/3/13 10:55
 */
@EnableSwagger2
@SpringBootApplication(scanBasePackages = {DataRoomConst.ScanPackage.COMPONENT, DatasetConstant.ScanPackage.COMPONENT, CommonConst.ScanPackage.COMPONENT})
@MapperScan(value = {DataRoomConst.ScanPackage.DAO, DatasetConstant.ScanPackage.DAO})
@ComponentScan(basePackages = {"com.gccloud.*"})
public class DataRoomApplication implements ServletContextInitializer {

    public static void main(String[] args) {

        SpringApplication.run(DataRoomApplication.class, args);
    }
    //添加websocket支持
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
    //设置websocket发送内容长度
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("org.apache.tomcat.websocket.textBufferSize","1024000");
    }
    /**
     * 分页插件
     *
     * @return PaginationInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
