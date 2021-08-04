package de.netbeacon.xenia.bot.utils.backend.action;

import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataObject;

import java.util.List;
import java.util.Map;

public class BackendActionResultBundle{

	private final Map<Class<?>, List<APIDataObject<?>>> map;

	protected BackendActionResultBundle(Map<Class<?>, List<APIDataObject<?>>> map){
		this.map = map;
	}

	public <T extends APIDataObject<T>> T get(Class<T> clazz){
		return get(clazz, 0);
	}

	public <T extends APIDataObject<T>> T get(Class<T> clazz, int pos){
		if(!map.containsKey(clazz))
			return null;
		if(map.get(clazz).size() <= pos)
			return null;
		return (T) map.get(clazz).get(pos);
	}

}
