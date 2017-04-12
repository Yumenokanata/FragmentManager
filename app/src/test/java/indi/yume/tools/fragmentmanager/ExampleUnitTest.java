package indi.yume.tools.fragmentmanager;

import org.junit.Test;

import indi.yume.tools.fragmentmanager.event.Action;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        final Subject<String> subject = PublishSubject.<String> create().toSerialized();
        Flowable<String> flowable = subject.toFlowable(BackpressureStrategy.BUFFER);

        flowable.subscribe(e -> System.out.println("onNext: " + e),
                t -> System.out.println("onError"),
                () -> System.out.println("onComplete"));

        subject.onNext("action1");
//        subject.onComplete();

        Flowable<String> flowable2 = subject.toFlowable(BackpressureStrategy.BUFFER);
        flowable2.subscribe(e -> System.out.println("onNext2: " + e),
                t -> System.out.println("onError2"),
                () -> System.out.println("onComplete2"));
        subject.onNext("action2");
        subject.onComplete();
        subject.retry();

        System.out.println("isCompelete: " + subject.hasComplete());
        System.out.println("hasObservers: " + subject.hasObservers());

        Flowable<String> flowable3 = subject.toFlowable(BackpressureStrategy.BUFFER);
        flowable3.subscribe(e -> System.out.println("onNext3: " + e),
                t -> System.out.println("onError3"),
                () -> System.out.println("onComplete3"));
        subject.onError(new Exception());
        subject.onComplete();

        System.out.println("isCompelete: " + subject.hasComplete());
        System.out.println("hasObservers: " + subject.hasObservers());
        System.out.println("hasThrowable: " + subject.hasThrowable());
    }

    @Test
    public void completableTest() {
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                e.onComplete();
            }
        })
                .doOnComplete(() -> System.out.println("doOnComplete"))
                .toSingleDefault("11")
                .toFlowable()
                .subscribe(s -> System.out.println(s));
    }
}