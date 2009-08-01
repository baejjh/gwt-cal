package com.bradrydzewski.gwt.calendar.client;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.event.DeleteEvent;
import com.bradrydzewski.gwt.calendar.client.event.DeleteHandler;
import com.bradrydzewski.gwt.calendar.client.event.HasDeleteHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasTimeBlockClickHandlers;
import com.bradrydzewski.gwt.calendar.client.event.TimeBlockClickEvent;
import com.bradrydzewski.gwt.calendar.client.event.TimeBlockClickHandler;
import com.bradrydzewski.gwt.calendar.client.util.AppointmentUtil;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is a base class all Calendar Views (i.e. Day view, month view, list view)
 * should build upon. It defines and or implements all required methods and
 * properties.
 * @author Brad Rydzewski
 */
public abstract class CalendarView extends Composite implements
        HasSelectionHandlers<AppointmentInterface>, HasOpenHandlers<AppointmentInterface>,
        HasDeleteHandlers<AppointmentInterface>, HasTimeBlockClickHandlers<Date>,
        HasValue<Appointment>, HasSettings {

    protected AbsolutePanel rootPanel = new AbsolutePanel();
    protected boolean layoutSuspended = false;
    protected boolean layoutPending = false;
    protected boolean sortPending = true;
    private Date date = new Date();
    private int days = 3;
    protected ArrayList<Appointment> appointments = new ArrayList<Appointment>();
    protected ArrayList<Appointment> multiDayAppointments = new ArrayList<Appointment>();
    protected Appointment selectedAppointment = null;
    private CalendarSettings settings = null;

    public CalendarView(CalendarSettings settings) {

        initWidget(rootPanel);
        this.settings = settings;
        sinkEvents(Event.ONMOUSEDOWN | Event.ONDBLCLICK | Event.KEYEVENTS);
    }

    public abstract void doLayout();

    public void suspendLayout() {
        layoutSuspended = true;
    }

    public void resumeLayout() {
        layoutSuspended = false;
        if (layoutPending) {
            doLayout();
        }
    }

    public CalendarSettings getSettings() {
        return settings;
    }

    public void setSettings(CalendarSettings settings) {
        this.settings = settings;
        doLayout();
    }

    public Date getDate() {
        return (Date)date.clone();
    }

    public void setDate(Date date, int days) {
        this.date = date;
        this.days = days;
        doLayout();
    }

    public void setDate(Date date) {
        this.date = date;
        doLayout();
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
        doLayout();
    }

    protected ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public int getAppointmentCount() {
        return appointments.size();
    }

    public AppointmentInterface getAppointmentAtIndex(int index) {
        return appointments.get(index);
    }

    public AppointmentInterface getSelectedAppointment() {
        return selectedAppointment;
    }

    public boolean selectNextAppointment() {

        if (getSelectedAppointment() == null) {
            return false;
        }
        int index = appointments.indexOf(getSelectedAppointment());
        if (index >= appointments.size()) {
            return false;
        }
        Appointment appt = appointments.get(index + 1);
        if (appt.isVisible() == false) {
            return false;
        }
        this.setValue(appt);
        return true;
    }

    public boolean selectPreviousAppointment() {
        if (getSelectedAppointment() == null) {
            return false;
        }
        int index = appointments.indexOf(getSelectedAppointment());
        if (index <= 0) {
            return false;
        }
        Appointment appt = appointments.get(index - 1);
        if (appt.isVisible() == false) {
            return false;
        }
        this.setValue(appt);
        return true;
    }

    public void setSelectedAppointment(Appointment appointment) {

        // add appointment if doesn't exist
        if (!appointments.contains(appointment)) {
            appointments.add(appointment);
        }

        // de-select currently selected appointment
        if (selectedAppointment != null) {
            selectedAppointment.setSelected(false);
        }

        // set newly selected appointment
        this.selectedAppointment = appointment;
        appointment.setSelected(true);
    }

    public void updateAppointment(Appointment appointment) {

        if(AppointmentUtil.isMultiDay(appointment)) {
            appointment.setMultiDay(true);
        }
        
        if (!appointments.contains(appointment)) {
            appointments.add(appointment);
        }
        
        //if it is a multi day appointment make sure it is on the list
        if(appointment.isMultiDay() && !multiDayAppointments.contains(appointment)) {
            multiDayAppointments.add(appointment);
        //if not, make sure it IS NOT on the list
        } else if(multiDayAppointments.contains(appointment)) {
            multiDayAppointments.remove(appointment);
        }

        sortPending = true;
        doLayout();
    }

    public void clearAppointments() {
        appointments.clear();
        multiDayAppointments.clear();
        doLayout();
    }

    public void removeAppointment(Appointment appointment, boolean fireEvents) {

        boolean commitChange = true;

        if (fireEvents) {
            commitChange = DeleteEvent.fire(this, getSelectedAppointment());
        }
        if (commitChange) {
            appointments.remove(appointment);
            multiDayAppointments.remove(appointment);
            selectedAppointment = null;
            sortPending = true;
            doLayout();
        }
    }

    public void removeAppointment(Appointment appointment) {
        removeAppointment(appointment, false);
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        
        /* this is what i need to do, but calcuation doens't work yet */
        /* so for now developer will need to manually set flag */
//        if(AppointmentUtil.isMultiDay(appointment)) {
//            appointment.setMultiDay(true);
//            multiDayAppointments.add(appointment);
//        }
        if(appointment.isMultiDay())
            multiDayAppointments.add(appointment);
        
        this.sortPending = true;

        doLayout();
    }

    public void addAppointments(ArrayList<Appointment> appointments) {
        for (Appointment appointment : appointments) {
            addAppointment(appointment);
        }
    }

    @Override
    public Appointment getValue() {
        return selectedAppointment;
    }

    @Override
    public void setValue(Appointment value) {
        setValue(value, true);
    }

    @Override
    public void setValue(Appointment value, boolean fireEvents) {

        Appointment oldValue = selectedAppointment;
        Appointment newValue = value;

        // de-select currently selected appointment
        if (oldValue != null) {
            oldValue.setSelected(false);

        }

        // set newly selected appointment
        selectedAppointment = newValue;
        newValue.setSelected(true);

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, newValue);
            if (newValue != oldValue) {
                SelectionEvent.fire(this, newValue);
            }
        }

    }

    @Override
    public HandlerRegistration addDeleteHandler(
            DeleteHandler<AppointmentInterface> handler) {
        return addHandler(handler, DeleteEvent.getType());
    }

    @Override
    public HandlerRegistration addTimeBlockClickHandler(
            TimeBlockClickHandler<Date> handler) {
        return addHandler(handler, TimeBlockClickEvent.getType());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Appointment> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<AppointmentInterface> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    @Override
    public HandlerRegistration addOpenHandler(OpenHandler<AppointmentInterface> handler) {
        return addHandler(handler, OpenEvent.getType());
    }
}
