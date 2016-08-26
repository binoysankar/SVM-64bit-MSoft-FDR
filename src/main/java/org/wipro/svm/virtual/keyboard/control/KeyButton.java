package org.wipro.svm.virtual.keyboard.control;

import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

public abstract class KeyButton extends Button implements LongPressable {

	private final static long DEFAULT_DELAY = 400;

	protected String keyText;

	private boolean movable, repeatable;

	protected int keyCode;

	protected Timeline buttonDelay;

	private ObjectProperty<EventHandler<? super KeyButtonEvent>> onLongPressed;

	private ObjectProperty<EventHandler<? super KeyButtonEvent>> onShortPressed;

	public KeyButton() {
		this(null, null, DEFAULT_DELAY);
	}

	public KeyButton(String label) {
		this(label, null, DEFAULT_DELAY);
	}

	public KeyButton(Node graphic) {
		this(null, graphic, DEFAULT_DELAY);
	}

	public KeyButton(String label, Node graphic) {
		this(label, graphic, DEFAULT_DELAY);
	}

	public KeyButton(String label, long delay) {
		this(label, null, delay);
	}

	public KeyButton(String label, Node graphic, long delay) {
		super(label, graphic);
		getStyleClass().add("key-button");
		initEventListener(delay > 0 ? delay : DEFAULT_DELAY);

	}

	protected abstract void initEventListener(long delay);

	protected void fireLongPressed() {
		fireEvent(new KeyButtonEvent(this, KeyButtonEvent.LONG_PRESSED));
	}

	protected void fireShortPressed() {
		fireEvent(new KeyButtonEvent(this, KeyButtonEvent.SHORT_PRESSED));
	}

	@Override
	public final void setOnLongPressed(EventHandler<? super KeyButtonEvent> eventhandler) {
		onLongPressedProperty().set(eventhandler);
	}

	@Override
	public final EventHandler<? super KeyButtonEvent> getOnLongPressed() {
		return onLongPressedProperty().get();
	}

	@Override
	public final ObjectProperty<EventHandler<? super KeyButtonEvent>> onLongPressedProperty() {
		if (onLongPressed == null) {
			onLongPressed = new SimpleObjectProperty<EventHandler<? super KeyButtonEvent>>() {

				@SuppressWarnings("unchecked")
				@Override
				protected void invalidated() {
					setEventHandler(KeyButtonEvent.LONG_PRESSED, (EventHandler<? super Event>) get());
				}
			};
		}
		return onLongPressed;
	}

	@Override
	public final void setOnShortPressed(EventHandler<? super KeyButtonEvent> eventhandler) {
		onShortPressedProperty().set(eventhandler);
	}

	@Override
	public final EventHandler<? super KeyButtonEvent> getOnShortPressed() {
		return onShortPressedProperty().get();
	}

	@Override
	public final ObjectProperty<EventHandler<? super KeyButtonEvent>> onShortPressedProperty() {
		if (onShortPressed == null) {
			onShortPressed = new SimpleObjectProperty<EventHandler<? super KeyButtonEvent>>() {

				@SuppressWarnings("unchecked")
				@Override
				protected void invalidated() {
					setEventHandler(KeyButtonEvent.SHORT_PRESSED, (EventHandler<? super Event>) get());
				}
			};
		}
		return onShortPressed;

	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

	public void addExtKeyCode(int keyCode, String label) {
	}

	public boolean isMovable() {
		return movable;
	}

	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}

}
