package jviewmda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
	
	class HandlerList {
		public List<EventHandler<ActionEvent>> list=new ArrayList<EventHandler<ActionEvent>>();
	}	
}

