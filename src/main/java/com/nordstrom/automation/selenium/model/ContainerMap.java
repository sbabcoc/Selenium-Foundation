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

/**
 * This is the abstract base class for all of the container map classes defined by <b>Selenium Foundation</b>.
 * <p>
 * <b>NOTE</b>: This class implements a read-only map; all methods that would alter the composition of the collection
 * (e.g. - {@link #put(Object, Object)}) result in {@link UnsupportedOperationException}.
 *
 * @param <V> the class of container objects collected by this map
 */
abstract class ContainerMap<V extends ComponentContainer> extends AbstractMap<Object, V> {

    protected ComponentContainer parent;
    protected Class<V> containerType;
    protected By locator;
    protected Method method;
    
    private List<WebElement> elements;
    private ContainerEntry<V>[] table;
    private Set<Map.Entry<Object, V>> entrySet;
    private int size;
    
    /**
     * Constructor for container map with parent, type, and locator
     * 
     * @param parent parent container
     * @param containerType container type
     * @param locator container context element locator
     */
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
            if (e.key == key || (key != null && key.equals(e.key))) {
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
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + parent.hashCode();
        result = PRIME * result + containerType.hashCode();
        result = PRIME * result + locator.hashCode();
        result = PRIME * result + elements.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContainerMap<?> other = (ContainerMap<?>) obj;
        if (!parent.equals(other.parent))
            return false;
        if (!containerType.equals(other.containerType))
            return false;
        if (!locator.equals(other.locator))
            return false;
        if (!elements.equals(other.elements))
            return false;
        return true;
    }
    
    /**
     * This class implements a container map entry.
     * <p>
     * The {@link ContainerMap#entrySet()} method returns a collection-view of the map, whose elements are of this
     * class. The only way to obtain a reference to a container map entry is from the iterator of this collection-view.
     * 
     * @param <V> the class of container object held by this entry
     */
    static class ContainerEntry<V extends ComponentContainer> implements Map.Entry<Object, V> {

        private ContainerMap<V> map;
        private RobustWebElement element;
        private ContainerEntry<V> next;
        private Object key;
        private V value;

        /**
         * Constructor for container map entry
         * 
         * @param map container map to which this entry belongs
         * @param element container context element
         * @param next link to the next container entry ('null' for final entry)
         */
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
        
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + map.hashCode();
            result = PRIME * result + element.hashCode();
            result = PRIME * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ContainerEntry<?> other = (ContainerEntry<?>) obj;
            if (!map.equals(other.map))
                return false;
            if (!element.equals(other.element))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
    }
    
    /**
     * This class defines an unordered collection of container map entries - a {@code set}.
     * <p>
     * <b>NOTE</b>: This class implements a read-only set; all methods that would alter the composition of the
     * collection (e.g. - {@link #remove}) result in {@link UnsupportOperationException}.
     */
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
            if (!(o instanceof Map.Entry)) {
                return false;
            }
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
    
    /**
     * This class implements a container map entry iterator.
     * <p>
     * <b>NOTE</b>: This class implements a read-only iterator; all methods that would alter the composition of the
     * collection (e.g. - {@link #remove}) result in {@link UnsupportOperationException}.
     */
    class ContainerEntryIterator implements Iterator<Map.Entry<Object, V>> {
        
        private ContainerEntry<V> next;
        private int index;
        
        /**
         * Constructor for a container map entry iterator.
         */
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
