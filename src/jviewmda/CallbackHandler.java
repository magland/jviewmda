package jviewmda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 *
 * @author magland
 */
public class CallbackHandler {
	Map<String,HandlerList> m_handlers=new HashMap<String,HandlerList>();
	
	public CallbackHandler() {
	}
	public void bind(String name,EventHandler<ActionEvent> handler) {
		if (!m_handlers.containsKey(name)) {
			m_handlers.put(name,new HandlerList());
		}
		m_handlers.get(name).list.add(handler);
	}
	public void trigger(String name) {
		trigger(name,new ActionEvent());
	}
	public void trigger(String name,ActionEvent evt) {
		if (!m_handlers.containsKey(name)) return;
		m_handlers.get(name).list.forEach(handler->{
			handler.handle(evt);
		});
	}
	public void scheduleTrigger(String name,int timeout) {
		scheduleTrigger(name,new ActionEvent(),timeout);
	}
	Set<String> m_scheduled_triggers=new HashSet<String>();
	public void scheduleTrigger(String name,ActionEvent evt,int timeout) {
		if (m_scheduled_triggers.contains(name)) return;
		m_scheduled_triggers.add(name);
		new Timeline(new KeyFrame(Duration.millis(timeout),e -> {
			m_scheduled_triggers.remove(name);
			trigger(name,evt);
		})).play();
		
	}
	
	class HandlerList {
		public List<EventHandler<ActionEvent>> list=new ArrayList<EventHandler<ActionEvent>>();
	}	
}

