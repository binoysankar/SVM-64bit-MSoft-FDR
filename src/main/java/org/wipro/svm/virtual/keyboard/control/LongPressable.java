package org.wipro.svm.virtual.keyboard.control;

import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

interface LongPressable {

	void setOnLongPressed(EventHandler<? super KeyButtonEvent> eventhandler);

	EventHandler<? super KeyButtonEvent> getOnLongPressed();

	ObjectProperty<EventHandler<? super KeyButtonEvent>> onLongPressedProperty();

	void setOnShortPressed(EventHandler<? super KeyButtonEvent> eventhandler);

	EventHandler<? super KeyButtonEvent> getOnShortPressed();

	ObjectProperty<EventHandler<? super KeyButtonEvent>> onShortPressedProperty();
}
