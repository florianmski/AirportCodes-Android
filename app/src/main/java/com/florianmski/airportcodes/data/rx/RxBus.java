package com.florianmski.airportcodes.data.rx;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public enum RxBus
{
    INSTANCE;

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object event)
    {
        bus.onNext(event);
    }

    public Observable<Object> toObserverable()
    {
        return bus;
    }

    public boolean hasObservers()
    {
        return bus.hasObservers();
    }
}
