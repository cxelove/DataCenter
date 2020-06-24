package com.ldchina.datacenter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import com.ldchina.datacenter.mina.MinaUdpServerHandler;
import com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd.LufftCommonCmdCodecFactory;
import com.ldchina.datacenter.mina.protocol.protocols.lufft_heartbeat.LufftHeartBeatCodecFactory;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.h2.tools.Server;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.ldchina.datacenter.mina.MinaTcpServerHandler;

@SpringBootApplication
//@EnableTransactionManagement //事务配置
//@EnableAsync //启用异步
public class MainApplication {
	private final static Logger log = LoggerFactory.getLogger(MainApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
	@Value("${databasePort}")
    public String dbPort;

    @PostConstruct
    private  void startH2Server() {
        try {
            Server h2Server=Server.createTcpServer(
                    new String[] {"-tcp", "-tcpAllowOthers","-tcpPort",dbPort}
            ).start();
            if (h2Server.isRunning(true)) {
                log.info("H2 server was started and is running at port "+dbPort+".");
            } else {
                throw new RuntimeException("Could not start H2 server.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start H2 server: ", e);
        }
    }
	
	
	@Value("${serverPort}")
    public int serverPort;

    @Bean
    public void minaUdpServer() throws IOException {

        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.setHandler(new MinaUdpServerHandler());

        Executor threadPool;
        threadPool = Executors.newCachedThreadPool();
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
     //   chain.addLast("logger", new LoggingFilter());
       // chain.addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        // chain.addLast("codec", new ProtocolCodecFilter(new ByteA));
        // chain.addLast("lufftComonCmdCodec", new ProtocolCodecFilter(new LufftCommonCmdCodecFactory()));
       // chain.addLast("lufftHeartBeatCodec", new ProtocolCodecFilter(new LufftHeartBeatCodecFactory()));

        chain.addLast("threadPool", new ExecutorFilter(threadPool));

        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReadBufferSize(4096);// 设置接收最大字节默认2048
        dcfg.setMaxReadBufferSize(65536);
        dcfg.setReceiveBufferSize(1024);// 设置输入缓冲区的大小
        dcfg.setSendBufferSize(1024);// 设置输出缓冲区的大小
        dcfg.setReuseAddress(true);// 设置每一个非主监听连接的端口可以重用

        acceptor.bind(new InetSocketAddress(serverPort));
        log.info("UDPServer listening on port " + serverPort);
    }
    @Bean
    public void minaTcpServer()  {
        // 4步操作
        //1 新建NioSocketAcceptor实例对象
        IoAcceptor acceptor = new NioSocketAcceptor();
        //线程池
        acceptor.getFilterChain()
                .addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        // 2.设置读取缓存大小
        acceptor.getSessionConfig().setReadBufferSize(2048);
        // 设置响应时常,读写通道均在10秒内无任何操作进如空闲状态
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 200);
        // 3.设置消息处理对象
        acceptor.setHandler(new MinaTcpServerHandler());
        // 设置所使用的的字符编码格式
        acceptor.getFilterChain().addLast(
                "gb2312",
                new ProtocolCodecFilter(new TextLineCodecFactory(
                        Charset.forName("GB2312")
                        , LineDelimiter.WINDOWS.getValue()
                        , LineDelimiter.WINDOWS.getValue()
                )));
        acceptor.getFilterChain().addLast(
                "utf8",
                new ProtocolCodecFilter(new TextLineCodecFactory(
                        Charset.forName("UTF8")
                        , LineDelimiter.WINDOWS.getValue()
                        , LineDelimiter.WINDOWS.getValue()
                )));
        // 4.绑定端口
        try {
            // 绑定端口开启服务
            acceptor.bind(new InetSocketAddress(serverPort));
            log.info("TcpServer listening on port " + serverPort);
        } catch (IOException e) {
            log.error("Data receiver port bind failed (" + serverPort + ").");
            e.printStackTrace();
        }
    } // end of main
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
