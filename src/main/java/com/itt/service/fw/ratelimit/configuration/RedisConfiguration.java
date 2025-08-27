package com.itt.service.fw.ratelimit.configuration;

//@Configuration
public class RedisConfiguration {

//
//	@Bean(name = "rate-limit-cache-manager")
//	public CacheManager cacheManager(final RedisProperties redisProperties) {
//		final var cacheManager = Caching.getCachingProvider().getCacheManager();
//		final var isCacheCreated = Optional.ofNullable(cacheManager.getCache(CACHE_NAME)).isPresent();
//
//		if (Boolean.FALSE.equals(isCacheCreated)) {
//			final var connectionUrl = String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());
//			final var configuration = new Config();
//			configuration.useSingleServer().setPassword(redisProperties.getPassword()).setAddress(connectionUrl);
//
//			var configMap = new HashMap<String, Object>();
//			configMap.put("redisson.config", configuration);
//
//			MutableConfiguration<Object, Object> cacheConfig = new MutableConfiguration<>();
//			cacheConfig.setStoreByValue(false); // avoid serialization if not needed
//			cacheConfig.setExpiryPolicyFactory(
//					CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 10))
//			);
//
//			cacheConfig.setManagementEnabled(false);
//			cacheConfig.setStatisticsEnabled(false);
//
//			cacheManager.createCache(CACHE_NAME, RedissonConfiguration.fromConfig(configuration, cacheConfig));
//		}
//		return cacheManager;
//	}
//
//	@Bean
//	ProxyManager<UUID> proxyManager(final CacheManager cacheManager) {
//		return new JCacheProxyManager<UUID>(cacheManager.getCache(CACHE_NAME));
//	}

}