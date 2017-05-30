package com.nordstrom.automation.selenium.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ComponentMap<K, V extends ComponentContainer & Map.Entry<K, V>> implements Map<K, V> {

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	static abstract class ComponentNode<V extends PageComponent> implements Map.Entry<Object, V> {
		
		private RobustWebElement element;
		private ComponentContainer parent;
		private Class<V> componentType;
		private V component;
		
		ComponentNode(RobustWebElement element, ComponentContainer parent, Class<V> componentType) {
			this.element = element;
			this.parent = parent;
			this.componentType = componentType;
		}
		
		@Override
		public V getValue() {
			if (component == null) {
				
			}
			return component;
		}

		@Override
		public V setValue(V value) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	static abstract class FrameNode<V extends Frame> implements Map.Entry<Object, V> {
		
	}
	
}
