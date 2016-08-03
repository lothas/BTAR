package com.MobileAnarchy.Android.Widgets.Joystick;

public interface JoystickMovedListener {
	public void OnMoved(double pan, double tilt);
	public void OnReleased();
	public void OnReturnedToCenter();
}
