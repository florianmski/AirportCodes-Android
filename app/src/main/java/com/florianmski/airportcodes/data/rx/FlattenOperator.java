package com.florianmski.airportcodes.data.rx;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class FlattenOperator<T> implements Observable.Operator<T, List<T>>
{
    @Override
    public Subscriber<? super List<T>> call(final Subscriber<? super T> subscriber)
    {
        return new Subscriber<List<T>>()
        {
            @Override
            public void onCompleted()
            {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e)
            {
                subscriber.onError(e);
            }

            @Override
            public void onNext(List<T> ts)
            {
                for(T t : ts)
                    subscriber.onNext(t);
            }
        };
    }
}