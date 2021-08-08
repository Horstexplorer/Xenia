package de.netbeacon.xenia.bot.utils.backend.action;

import de.netbeacon.utils.concurrency.action.ExecutionAction;
import de.netbeacon.utils.concurrency.action.imp.SupplierExecutionAction;
import de.netbeacon.xenia.backend.client.objects.internal.objects.APIDataObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BackendActions{

	public static ExecutionAction<BackendActionResultBundle> allOf(List<ExecutionAction<? extends APIDataObject<?>>> actions){
		Supplier<BackendActionResultBundle> fun = () -> {
			var inner = ExecutionAction.accumulate(actions, Collectors.toList());
			Map<Class<?>, List<APIDataObject<?>>> map = new HashMap<>();
			for(var o : inner.execute()){
				if(!map.containsKey(o.getClass())){
					map.put(o.getClass(), new ArrayList<>());
				}
				map.get(o.getClass()).add(o);
			}
			return new BackendActionResultBundle(map);
		};
		return new SupplierExecutionAction<>(fun);
	}

}
