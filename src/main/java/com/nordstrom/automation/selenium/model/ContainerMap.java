package com.nordstrom.automation.selenium.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.nordstrom.common.base.UncheckedThrow;

abstract class ContainerMap<V extends ComponentContainer> extends AbstractMap<Object, V> {
    
    protected ComponentContainer parent;
    protected Class<V> containerType;
    protected By locator;
    protected Method method;
    
    private List<WebElement> elements;
    private ContainerEntry<V>[] table;
    private Set<Map.Entry<Object, V>> entrySet;
    private int size;
    
    @SuppressWarnings("unchecked")
    ContainerMap(ComponentContainer parent, Class<V> containerType, By locator) {
        Objects.requireNonNull(parent, "[parent] must be non-null");
        Objects.requireNonNull(containerType, "[containerType] must be non-null");
        Objects.requireNonNull(locator, "[locator] must be non-null");
        
        ComponentContainer.verifyCollectible(containerType);
        
        this.parent = parent;
        this.containerType = containerType;
        this.locator = locator;
        
        method = ComponentContainer.getKeyMethod(containerType);
        
        elements = parent.findElements(locator);
        size = elements.size();
        table = new ContainerEntry[size];
        
        int i = size;
        ContainerEntry<V> next = null;
        while (i-- > 0) {
            next = table[i] = new ContainerEntry<>(this, (RobustWebElement) elements.get(i), next);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        if (value.getClass() == containerType) {
            V container = (V) value;
            SearchContext context = container.getContext();
            return elements.contains(context);
        }
        return false;
    }
    
    @Override
    public Set<Map.Entry<Object, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new ContainerEntrySet();
        }
        return Collections.unmodifiableSet(entrySet);
    }
    
    /**
     * Get table entry for the specified key.
     * 
     * @param key key of desired entry
     * @return entry for the specified key; 'null' if not found
     */
    final ContainerEntry<V> getEntry(Object key) {
        if (table.length == 0) {
            return null;
        }
        ContainerEntry<V> e;
        for (e = table[0]; e != null; e = e.next) {
            Object k = e.key;
            if (k == key || (key != null && key.equals(k))) {
                break;
            }
        }
        return e;
    }
    
    /**
     * Get array of constructor argument types.
     * 
     * @return array of constructor argument types
     */
    Class<?>[] getArgumentTypes() {
        return ComponentContainer.getCollectibleArgs();
    }
    
    /**
     * Get array of constructor argument values for the specified context element.
     * 
     * @param element container map context element
     * @return array of constructor argument values
     */
    Object[] getArguments(WebElement element) {
        return new Object[] {(RobustWebElement) element, parent};
    }
    
    static class ContainerEntry<V extends ComponentContainer> implements Map.Entry<Object, V> {
        private ContainerMap<V> map;
        private RobustWebElement element;
        private ContainerEntry<V> next;
        private Object key;
        private V value;

        ContainerEntry(ContainerMap<V> map, RobustWebElement element, ContainerEntry<V> next) {
            this.map = map;
            this.element = element;
            this.next = next;
            
            try {
                key = map.method.invoke(null, element);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw UncheckedThrow.throwUnchecked(e);
            }
        }
        
        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public V getValue() {
            V v = value;
            if (v == null) {
                Class<?>[] argumentTypes = map.getArgumentTypes();
                Object[] arguments = map.getArguments(element);
                v = ComponentContainer.newContainer(map.containerType, argumentTypes, arguments);
                v = v.enhanceContainer(v);
                value = v;
            }
            return v;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
    
    class ContainerEntrySet extends AbstractSet<Map.Entry<Object, V>> {
        
        @Override
        public final int size()                 {
            return size;
        }
        
        @Override
        public final Iterator<Map.Entry<Object, V>> iterator() {
            return new ContainerEntryIterator();
        }
        
        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            ContainerEntry<V> candidate = getEntry(key);
            return candidate != null && candidate.equals(e);
        }
        
        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
    }
    
    class ContainerEntryIterator implements Iterator<Map.Entry<Object, V>> {
        private ContainerEntry<V> next;
        private int index;
        
        ContainerEntryIterator() {
            findNextEntry();
        }

        @Override
        public final boolean hasNext() {
            return next != null;
        }

        @Override
        public ContainerEntry<V> next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            
            ContainerEntry<V> current = next;
            next = next.next;
            findNextEntry();
            
            return current;
        }
        
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
        
        /**
         * Find the next non-null entry (if one exists).
         * <p>
         * If [next] is initially 'null' and [index] is still within bounds, this method performs an index-based walk
         * of the table to find the next non-null entry. Upon completion, [index] will point one position beyond the
         * the last evaluated entry.
         */
        private void findNextEntry() {
            while (next == null && index < table.length) {
                next = table[index++];
            }
        }
    }
}
