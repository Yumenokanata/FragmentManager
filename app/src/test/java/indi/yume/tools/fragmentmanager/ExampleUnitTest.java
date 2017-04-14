package indi.yume.tools.fragmentmanager;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import indi.yume.tools.fragmentmanager.event.Action;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
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

    @Test
    public void completableTest2() {
        Completable.complete()
                .andThen(Completable.fromAction(() -> System.out.println("Completable2")))
                .andThen(Completable.fromAction(() -> System.out.println("Completable3")))
                .andThen(Completable.error(new Exception()))
                .andThen(Completable.fromAction(() -> System.out.println("Completable4")))
                .subscribe(() -> System.out.println("complete"),
                        t -> System.out.println("onError: " + t));
    }

    @Test
    public void completableTest3() {
        final Subject<String> subject = PublishSubject.<String> create().toSerialized();

        Completable.complete()
                .andThen(
        subject.share()
                .doOnNext(s -> System.out.println("subscribe1 next: " + s))
                .first("default")
                .flatMapCompletable(s -> {
                    System.out.println("subscribe1 flatMapCompletable: " + s);
                    return Completable.create(e -> {
                        Thread.sleep(500);
                        e.onComplete();
                    })
                            .subscribeOn(Schedulers.newThread());
                })
                .doOnEvent(s -> System.out.println("subscribe1 event: " + s)))
                .subscribe(() -> System.out.println("subscribe1 complete: "),
                        t -> System.out.println("subscribe1 error: " + t));
        subject.share()
                .subscribe(s -> System.out.println("subscribe2 next: " + s),
                        t -> System.out.println("subscribe2 error: " + t));

        subject.onNext("1");
        subject.onNext("2");
//        subject.onComplete();

        System.out.println("isCompelete: " + subject.hasComplete());
        System.out.println("hasObservers: " + subject.hasObservers());
        System.out.println("hasThrowable: " + subject.hasThrowable());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void subjectTest() {
        final Subject<String> subject = PublishSubject.<String> create().toSerialized();

        subject.subscribe(s -> System.out.println("subscribe1 next: " + s),
                t -> System.out.println("subscribe1 error: " + t));
        subject
                .retry()
                .subscribe(s -> System.out.println("subscribe2 next: " + s),
                t -> System.out.println("subscribe2 error: " + t));

        subject.onNext("1");
        subject.onError(new Exception());
        subject.onNext("2");

        System.out.println("isCompelete: " + subject.hasComplete());
        System.out.println("hasObservers: " + subject.hasObservers());
        System.out.println("hasThrowable: " + subject.hasThrowable());
    }
}