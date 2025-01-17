package zxframe.cache.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import zxframe.cache.mgr.CacheModelManager;
import zxframe.config.ZxFrameConfig;
import zxframe.jpa.model.DataModel;
import zxframe.util.JsonUtil;
import zxframe.util.SerializeUtils;
/**
 * 数据本地缓存
 * 设计：存储不经常改变的数据，定时清理
 * @author 周璇
 */
@Component
public class LocalCacheManager {
	private static final Logger logger = LoggerFactory.getLogger(LocalCacheManager.class);  
	public static CacheManager  ehcache=null;
	static {
		ehcache = CacheManager.create();
	}
	public Object get(String group,String key) {
		DataModel dm = CacheModelManager.getDataModelByGroup(group);
		Object value = null;
		if(dm.isLcCache()) {
			Cache cache = getCache(group);
			Element element = cache.get(key);
			if(element!=null) {
				if(dm.isLcCacheDataClone()) {
					value =SerializeUtils.deSerialize((byte[]) element.getObjectValue());
				}else {
					value=element.getObjectValue();
				}
			}
			if(ZxFrameConfig.showlog) {
				logger.info("ehcache get group:"+group+" key:"+key+" , value:"+JsonUtil.obj2Json(value)+" lcacheSize:"+cache.getSize());
			}
		}
		return value;
	}
	public void put(String group,String key,Object value) {
		if(value==null) {
			return;
		}
		DataModel dm = CacheModelManager.getDataModelByGroup(group);
		if(dm.isLcCache()) {
			Cache cache = getCache(group);
			Element element=null;
			if(dm.isLcCacheDataClone()) {
				element = new Element(key,SerializeUtils.serialize(value));
			}else {
				element = new Element(key,value);
			}
			cache.put(element);
			if(ZxFrameConfig.showlog) {
				logger.info("ehcache put group:"+group+" key:"+key+" , value:"+JsonUtil.obj2Json(value)+" lcacheSize:"+cache.getSize());
			}
		}
		
	}
	public void remove(String group,String key) {
		DataModel dm = CacheModelManager.getDataModelByGroup(group);
		if(dm.isLcCache()) {
			Cache cache = getCache(group);
			cache.remove(key);
			if(ZxFrameConfig.showlog) {
				logger.info("ehcache remove group:"+group+" key:"+key+" lcacheSize:"+cache.getSize());
			}
		}
	}
	public void remove(String group){
		DataModel dm = CacheModelManager.getDataModelByGroup(group);
		if(dm.isLcCache()) {
			Cache cache = getCache(group);
			cache.removeAll();
			if(ZxFrameConfig.showlog) {
				logger.info("ehcache remove group:"+group+" lcacheSize:"+cache.getSize());
			}
		}
	}
	public Cache getCache(String group) {
		Cache cache = ehcache.getCache(group);
		if(cache==null) {
			ehcache.addCache(group);
			cache = ehcache.getCache(group);
			logger.info("ehcache cache init :"+cache.toString());
		}
		return cache;
	}
}
