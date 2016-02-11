package com.kakao.izzy.rxjavatraining;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.kakao.izzy.rxjavatraining.databinding.ActivityMainBinding;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

// 참고 자료
//    https://medium.com/@LIP/rxjava-29cfb3ceb4ca#.8z3zricib

// 사용된 기술
// 1. RxJava, RxAndroid
// 2. android.databinding
// 3. retrolambda

public class MainActivity extends AppCompatActivity {

    static final String TAG = "TAG_IZZY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        // -- Data Binding 사용 방법
        // onCreate In Activity -> LayoutNameBinding binding = DataBindingUtil.setContentView(this, ..);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // onViewCreated In Fragment
        // LayoutNameBinding binding = DataBindingUtil.bind(..);
        // --------------------------------------------------------




//        RxTextView.textChanges(bindin)

//        helloRxWorld();
//        simplerCode();
//        transformation();
//        operators();
    }

    // 1. 내가 무슨일이 일어나는지 최대한 정석적인 흐름의 메소드
    private void helloRxWorld() {
        Observable<String> myObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello, Rx World!");
                subscriber.onCompleted();
                // 이 Observable은 onNext에 담긴 내용을 발행한 뒤 종료된다. 그리고.. 이 데이터를 소비하는 Subsriber를 생성해보자.
            }
        });

        Subscriber<String> mySubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, s);
            }
        };

        // 이제 myObservable과 mySubscriber를 가졌으며 subsribe()를 사용하여 서로를 연결시킬 수 있다..
        myObservable.subscribe(mySubscriber);
        // 구독(subscription)을 만들면 myObservable은 subscriber의 onNext()와 onComplete() 메소드를 호출한다. 그 결과 mySubscriber는 Log를 출력한뒤 종료된다..
    }

    // 2. 1.의 코드를 좀 더 간편하게 만든 Simpler Code
    private void simplerCode() {
        // 1. 에서 단순히 Log만 출력하게 하는 것인데 너무 많은 상용구(boilerplate)코드를 만들었다

        // 1) 일단 Observable을 간소화하자. RxJava는 일반적인 작업을 위한 Observable 생성 메소드를 여러 개 내장하고 있다. 이 경우에는 Observable.just()가 아이템을 하나 발행한 뒤 종료한다.
        Observable<String> myObservable = Observable.just("Hello, Rx Simple World 1");
        // (엄밀하게 말하면, Observable.just() 는 1. 의 초기 코드와 정확하게 동일하지 않는다.)

        // 2) 불필요하게 장황한 Subscriber를 다뤄보자. 우리는 onCompleted()와 onError() 둘 다 상관하지 않는다. 그 대신에 우리는 onNext()에서 무엇을 수행할지를 정의하는 더 간단한 클래스를 사용할 수 있다.
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d(TAG, s);
            }
        };

        // 액션들은 Subscriber의 각 파트를 정의할 수 있다. Observable.subscribe()는 onNext(), onError() 그리고 onComplete()를 대신할 하나, 둘 또는 세 개의 액션 파라미터를 다룰 수 있다.
        // 즉, onCompletedAction, onErrorAction을 만들어 넣을 수 있다.
        Action0 onCompletedAction = new Action0() {
            @Override
            public void call() {

            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        };
        myObservable.subscribe(onNextAction, onErrorAction, onCompletedAction);

        // 하지만, 1.의 코드에서는 onNextAction만 필요하므로 제거 하고 onNextAction만 넣는다.
        myObservable.subscribe(onNextAction);

        // 3) 이제 메소드 호출을 서로 연쇄(chaining)시켜 변수들을 제거한다.
        Observable.just("Hello, Rx Simple World 2")
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d(TAG, s);
                    }
                });

        // 4) 마지막으로 흉칙한 모습의 Action1 코드를 제거하기 위한 Java8의 lambda를 이용하자.
        Observable.just("Hello, Rx Simple World 3")
                .subscribe(s -> Log.d(TAG, s));
    }

    // 3. 이제 양념을 조금 쳐보자. Hello world 출력에 나의 서명을 덧붙이고 싶다고 하자. 한가지 가능성은 Observer를 바꿔보는 것이다.
    private void transformation() {
        Observable.just("hello world! -Izzy")
                .subscribe(s -> Log.d(TAG, s));

        // 위와 같은 경우는 내가 나의 Observable을 제어할 수 있는 경우에는 잘 될 것이다. 하지만 이것이 보장이 되지 않는 경우도 있다.
        // - 외부 라이브러리를 사용하는 경우에는? 또 다른 잠재적인 문제는 : Observable을 여러 장소에서 사용하지만 가끔만 서명을 추가하고 싶다면 어쩌나?
        // 그럼 대신 우리의 Subscriber를 변경해보는 것은 어떨까?

        Observable.just("Hello world!")
                .subscribe(s -> Log.d(TAG, s + " -Izzy"));

        // 위와 같은 경우도 다른 이유들로 인해 여전히 만족스럽지 않다. : Subscriber는 메인 스레드에서 동작해야 할 수도 있으므로 최대한 가벼운 상태로 두고 싶다.
        // 더 개념적인 레벨에서, Subsriber들은 반응(reacts)하기로 되어 있는 것이지 변화(mutates)시키는 것이 아니다.

        // 그렇다면, 다른 중간 과정에서 "hello world!"를 변형시킬 수 있다면 쿨해지지 않을까?
    }

    // 4. Introducing Operators
    // 이제 아이템 변형(transformation) 문제를 조작자(operator)로 해결하는 방법을 보자.
    // Operator는 발행된 item들을 원천인 Observable과 최종의 Subscriber 사이에서 조작하기 위해 사용될 수 있다.
    // RxJava에는 수많은 operator들이 있다. 하지만 유용한 것에 먼저 집중하는 것이 가장 좋다.

    // 3. 과 같은 상황에서는 map() operator를 하나의 발행된 아이템을 다른 것으로 변형하는데 사용할 수 있다.
    private void operators() {
        Observable.just("hello world")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s + " -Izzy";
                    }
                })
                .subscribe(s -> Log.d(TAG, s));

        // 위의 코드도 람다로 단순화시킬 수 있다.
        Observable.just("hello world")
                .map(s -> s + " -Izzy")
                .subscribe(s -> Log.d(TAG, s));

        // 결론으로, map() operator는 기본적으로 아이템을 변형시키는 Observable이다. 우리는 map()호출을 원하는 만큼 연쇄시켜 마지막 Subscriber에서 소모가능한 형태로 데이터를 완벽하게 연마시킬 수 있다.
    }

    // map() 의
    private void moreOnMap() {
        //


    }

    private void create() {
        Observable.create(subscriber -> {
            subscriber.onNext("안녕");
            subscriber.onNext("잘가");
            subscriber.onCompleted();
        }).subscribe(
                System.out::println,
                Void -> System.out.println("onCompleted")
        );
    }
}
