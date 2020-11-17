package com.yang.shortvideo.sharding.config;

import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SharingConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${spring.cloud.nacos.config.server-addr}")
	private String serverAaddr;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		String rule = "/orchestration-sharding-data-table/config/schema/logic_db/rule";
		String datasource = "/orchestration-sharding-data-table/config/schema/logic_db/datasource";
		String dataId = rule.replace("/", ".");
		String dataId2 = datasource.replace("/", ".");
		String group = "sharding-jdbc";
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, serverAaddr);
		try {
			ConfigService configService = NacosFactory.createConfigService(properties);
			configService.addListener(dataId, group, new Listener() {

				@Override
				public Executor getExecutor() {
					return null;
				}

				@Override
				public void receiveConfigInfo(final String configInfo) {
					DataChangedEventListener dataChangedEventListener = NacosRegistryCenter.listeners
							.get(".orchestration-sharding-data-table.config.schema");
					dataChangedEventListener
							.onChange(new DataChangedEvent(rule, configInfo, DataChangedEvent.ChangedType.UPDATED));
				}
			});
			configService.addListener(dataId2, group, new Listener() {

				@Override
				public Executor getExecutor() {
					return null;
				}

				@Override
				public void receiveConfigInfo(final String configInfo) {
					DataChangedEventListener dataChangedEventListener = NacosRegistryCenter.listeners
							.get(".orchestration-sharding-data-table.config.schema");
					dataChangedEventListener
							.onChange(new DataChangedEvent(datasource, configInfo, DataChangedEvent.ChangedType.UPDATED));
				}
			});
		} catch (NacosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
