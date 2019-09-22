package net.opentsdb;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;
import net.opentsdb.core.Internal;
import net.opentsdb.uid.UniqueId;
import org.hbase.async.Bytes;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        Test.testFlag(1, 1568482200000L);
    }


    public static void testFlag(long value, long timestamp) {
        final byte[] v;
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
            v = new byte[] { (byte) value };
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            v = Bytes.fromShort((short) value);
        } else if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
            v = Bytes.fromInt((int) value);
        } else {
            v = Bytes.fromLong(value);
        }
        final short flags = (short) (v.length - 1);
        final byte[] qualifier = Internal.buildQualifier(timestamp, flags);
        System.out.println(Arrays.toString(v));
        System.out.println(Arrays.toString(qualifier));
    }


    private static void test() throws InterruptedException {
        out.println(Thread.currentThread().getName());

        Deferred<String> deferred = new Deferred<>().addBoth(str -> {
            out.println(Thread.currentThread().getName() + " 1 " + str.toString());
            return str;
        }).addBothDeferring(str -> {
            out.println("开始睡眠5秒钟");
            Thread.sleep(5000);
            out.println(Thread.currentThread().getName() + " 2 " + str);

            Deferred<String> d = new Deferred<>().addBoth(s -> {
                out.println(Thread.currentThread().getName() + " 3 " + s.toString());
                return s.toString();
            });

            Executors.newSingleThreadExecutor().execute(() -> {
                out.println(Thread.currentThread().getName() + " started.");
                d.callback("hello");
            });
            return d;
        }).addBoth(str -> {
            out.println(Thread.currentThread().getName() + " 4 " + str.toString());
            return str;
        }).addErrback(str -> {
            out.println(str.toString());
            return str;
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            out.println(Thread.currentThread().getName() + " started.");
//            deferred.callback("hello");
            deferred.callback(new NullPointerException("error."));
        });

        out.println(Thread.currentThread().getName() + " sleeping...");
        TimeUnit.MINUTES.sleep(1);
    }

}
