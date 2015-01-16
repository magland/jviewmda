package jviewmda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 *
 * @author magland
 */
public class CallbackHandler {
	Map<String,CallbackList> m_callbacks=new HashMap<>();
	
	public CallbackHandler() {
	}
	public void bind(String name,Runnable callback) {
		if (!m_callbacks.containsKey(name)) {
			m_callbacks.put(name,new CallbackList());
		}
		m_callbacks.get(name).list.add(callback);
	}
	public void trigger(String name) {
		if (!m_callbacks.containsKey(name)) return;
		m_callbacks.get(name).list.forEach(callback->{
			callback.run();
		});
	}
	Set<String> m_scheduled_triggers=new HashSet<String>();
	public void scheduleTrigger(String name,int timeout) {
		if (m_scheduled_triggers.contains(name)) return;
		m_scheduled_triggers.add(name);
		new Timeline(new KeyFrame(Duration.millis(timeout),e -> {
			m_scheduled_triggers.remove(name);
			trigger(name);
		})).play();
		
	}
	
	class CallbackList {
		public List<Runnable> list=new ArrayList<>();
	}	
}

