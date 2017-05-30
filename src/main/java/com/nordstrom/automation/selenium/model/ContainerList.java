package com.nordstrom.automation.selenium.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

import com.nordstrom.common.base.UncheckedThrow;

abstract class ContainerList<E extends ComponentContainer> implements List<E> {

	protected ComponentContainer parent;
	protected Class<E> containerType;
	protected List<WebElement> elementList;
	protected List<E> containerList;
	protected List<E> immutableView;
	
	ContainerList(ComponentContainer parent, Class<E> containerType, List<WebElement> elementList) {
		if (parent == null) throw new IllegalArgumentException("Parent must be non-null");
		if (containerType == null) throw new IllegalArgumentException("Container type must be non-null");
		if (elementList == null) throw new IllegalArgumentException("Element list must be non-null");
		
		this.parent = parent;
		this.containerType = containerType;
		this.elementList = elementList;
		this.containerList = new ArrayList<>(elementList.size());
		for (int i = 0; i < elementList.size(); i++) {
			containerList.add(null);
		}
		this.immutableView = Collections.unmodifiableList(this);
	}
	
	@Override
	public int size() {
		return containerList.size();
	}

	@Override
	public boolean isEmpty() {
		return containerList.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return immutableView.iterator();
	}

	@Override
	public Object[] toArray() {
		return immutableView.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return immutableView.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return immutableView.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return immutableView.remove(o);
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) return false;
		if (o.getClass() == containerType) {
			WebElement element = ((WrapsElement) o).getWrappedElement();
			for (WebElement thisElement : elementList) {
				if (thisElement.equals(element)) return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return immutableView.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return immutableView.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return immutableView.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return immutableView.retainAll(c);
	}

	@Override
	public void clear() {
		immutableView.clear();
	}

	@Override
	public E get(int index) {
		E container = containerList.get(index);
		if (container == null) {
			try {
				Constructor<E> ctor = containerType.getConstructor(getArgumentTypes());
				container = ctor.newInstance(getArguments(index));
				containerList.set(index, container.enhanceContainer(container));
			} catch (NoSuchMethodException | SecurityException | InstantiationException |
					IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw UncheckedThrow.throwUnchecked(e);
			}
		}
		return container;
	}

	@Override
	public E set(int index, E element) {
		return immutableView.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		immutableView.add(index, element);
	}

	@Override
	public E remove(int index) {
		return immutableView.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return containerList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return containerList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return immutableView.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return immutableView.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return immutableView.subList(fromIndex, toIndex);
	}
	
	abstract Class<?>[] getArgumentTypes();
	abstract Object[] getArguments(int index);

}
