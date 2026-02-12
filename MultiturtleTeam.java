import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class MultiTurtleTeam {

    // ====== ปรับความเร็วตรงนี้ ======
    static final int DRAW_SPEED = 6;      // แล้วแต่ Turtle.java ของนาย
    static final int RENDER_DELAY_MS = 4; // ช้าลง = เพิ่ม, เร็วขึ้น = ลด

    @FunctionalInterface
    interface Cmd { void apply(Turtle t); }

    static class Step {
        final Cmd cmd;
        final boolean end;
        Step(Cmd cmd, boolean end) { this.cmd = cmd; this.end = end; }
        static Step of(Cmd cmd) { return new Step(cmd, false); }
        static Step end() { return new Step(null, true); }
    }

    // Proxy: enqueue คำสั่งแทนการวาดทันที (กันปัญหา GUI ไม่ thread-safe) :contentReference[oaicite:1]{index=1}
    static class TurtleProxy {
        private final BlockingQueue<Step> q;
        TurtleProxy(BlockingQueue<Step> q) { this.q = q; }

        private void put(Cmd c) {
            try { q.put(Step.of(c)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        void finish() {
            try { q.put(Step.end()); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        void speed(int s) { put(t -> t.speed(s)); }
        void width(double w) { put(t -> t.width(w)); }
        void penColor(String c) { put(t -> t.penColor(c)); }
        void up() { put(Turtle::up); }
        void down() { put(Turtle::down); }
        void setPosition(double x, double y) { put(t -> t.setPosition(x, y)); }
        void setDirection(double deg) { put(t -> t.setDirection(deg)); }
        void forward(double d) { put(t -> t.forward(d)); }
        void left(double deg) { put(t -> t.left(deg)); }
        void right(double deg) { put(t -> t.right(deg)); }
        void dot(String color, int size) { put(t -> t.dot(color, size)); }
    }

    static void sleepMs(int ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static class Producer extends Thread {
        final CountDownLatch go;
        final Runnable job;
        Producer(String name, CountDownLatch go, Runnable job) {
            super(name);
            this.go = go;
            this.job = job;
        }
        @Override public void run() {
            try { go.await(); job.run(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    // ===== scripts: ตาม demo ใน Turtle.java =====

    // bob1: จุดหิมะ 4 สี (เหมือนเดิม)
    static Runnable dotsScript(TurtleProxy t) {
        return () -> {
            try {
                t.speed(DRAW_SPEED);
                t.width(1);

                Random r = new Random();

                for (int i = 0; i < 80; i++) {
                    double rx = -360 + r.nextDouble() * 760;
                    double ry = -250 + r.nextDouble() * 520;
                    t.up(); t.setPosition(rx, ry); t.down(); t.dot("white", 4);
                }
                for (int i = 0; i < 80; i++) {
                    double rx = -360 + r.nextDouble() * 760;
                    double ry = -250 + r.nextDouble() * 520;
                    t.up(); t.setPosition(rx, ry); t.down(); t.dot("red", 6);
                }
                for (int i = 0; i < 80; i++) {
                    double rx = -360 + r.nextDouble() * 760;
                    double ry = -250 + r.nextDouble() * 520;
                    t.up(); t.setPosition(rx, ry); t.down(); t.dot("gold", 5);
                }
                for (int i = 0; i < 80; i++) {
                    double rx = -360 + r.nextDouble() * 760;
                    double ry = -250 + r.nextDouble() * 520;
                    t.up(); t.setPosition(rx, ry); t.down(); t.dot("midnightblue", 3);
                }
            } finally {
                t.finish();
            }
        };
    }

    // bob2: ฐานเขียว + โบว์ + ดาว
    static Runnable greenBowStarScript(TurtleProxy t) {
        return () -> {
            try {
                t.speed(DRAW_SPEED);

                t.penColor("darkgreen");
                t.width(90);
                t.up();
                t.setPosition(-80, -170);
                t.setDirection(0);
                t.down();
                for (int i = 0; i < 4; i++) {
                    t.forward(100);
                    t.left(90);
                }

                t.setPosition(-80, -170);
                t.up();
                t.forward(50);
                t.right(90);
                t.forward(50);
                t.right(180);

                // โบว์
                t.forward(10);
                t.down();
                t.width(20);
                t.penColor("orange");
                t.forward(90);
                t.left(90);
                t.forward(90);
                t.right(180);
                t.forward(180);
                t.right(180);
                t.forward(90);
                t.right(90);
                t.forward(90);
                t.left(45);

                t.width(20);
                t.penColor("yellow");
                t.forward(40);
                t.left(180);
                t.forward(40);
                t.right(45);
                t.forward(40);
                t.right(180);
                t.forward(40);
                t.right(45);
                t.forward(40);
                t.up();

                // Star หิมะ
                t.right(45);
                t.forward(290);
                t.left(90);
                t.forward(190);
                t.down();
                t.penColor("yellow");
                t.width(9);
                for (int i = 0; i < 5; i++) {
                    t.forward(60);
                    t.right(144);
                }
            } finally {
                t.finish();
            }
        };
    }

    // bob3: กล่องแดง + ริบบิ้นน้ำเงิน
    static Runnable boxRibbonScript(TurtleProxy t) {
        return () -> {
            try {
                t.speed(DRAW_SPEED);

                // state ก่อนเข้า //กล่อง (ที่นายบอกว่าตรงแล้ว)
                t.up();
                t.setPosition(288.2842712474619, 188.28427124746196);
                t.setDirection(90);

                // กล่อง
                t.up();
                t.right(180);
                t.forward(360);
                t.right(90);
                t.forward(170);
                t.right(180);
                t.down();
                t.penColor("firebrick");
                t.width(90);
                for (int i = 0; i < 4; i++) {
                    t.forward(120);
                    t.left(90);
                }

                // ริบบิ้น
                t.up();
                t.forward(60);
                t.right(90);
                t.forward(38);
                t.right(180);
                t.penColor("midnightblue");
                t.width(27);
                t.down();
                t.forward(110);
                t.right(90);
                t.forward(100);
                t.setDirection(180);
                t.forward(200);
                t.right(180);
                t.forward(100);
                t.left(90);
                t.forward(100);
                t.right(90);
                t.width(14);
                t.forward(40);
                t.right(180);
                t.forward(80);
                t.right(180);
                t.forward(40);
                t.left(90);
                t.forward(50);

                t.up();
                t.forward(300);

            } finally {
                t.finish();
            }
        };
    }

    // renderer: สลับคิววาด round-robin ให้ “เห็นพร้อมกัน”
    static void renderRoundRobin(BlockingQueue<Step>[] qs, Turtle[] turtles) {
        boolean[] done = new boolean[qs.length];
        int doneCount = 0;

        while (doneCount < qs.length) {
            boolean progressed = false;

            for (int i = 0; i < qs.length; i++) {
                if (done[i]) continue;

                Step s = qs[i].poll();
                if (s == null) continue;

                progressed = true;

                if (s.end) {
                    done[i] = true;
                    doneCount++;
                } else {
                    s.cmd.apply(turtles[i]); // เรียก forward/left/right/dot ฯลฯ (ตามชุดคำสั่งเต่า) :contentReference[oaicite:2]{index=2}
                    sleepMs(RENDER_DELAY_MS);
                }
            }

            if (!progressed) sleepMs(1);
        }
    }

    public static void main(String[] args) throws Exception {
        Turtle.setCanvasSize(800, 600);
        Turtle.bgcolor("lightblue");

        Turtle bob1 = new Turtle();
        Turtle bob2 = new Turtle();
        Turtle bob3 = new Turtle();

        BlockingQueue<Step> q1 = new LinkedBlockingQueue<>();
        BlockingQueue<Step> q2 = new LinkedBlockingQueue<>();
        BlockingQueue<Step> q3 = new LinkedBlockingQueue<>();

        TurtleProxy p1 = new TurtleProxy(q1);
        TurtleProxy p2 = new TurtleProxy(q2);
        TurtleProxy p3 = new TurtleProxy(q3);

        CountDownLatch go = new CountDownLatch(1);

        Thread tDots  = new Producer("bob1-dots",  go, dotsScript(p1));
        Thread tGreen = new Producer("bob2-star",  go, greenBowStarScript(p2));
        Thread tBox   = new Producer("bob3-box",   go, boxRibbonScript(p3));

        tDots.start();
        tGreen.start();
        tBox.start();

        go.countDown(); // ✅ ปล่อยพร้อมกันทั้งหมด

        // ✅ วาดพร้อมกันตั้งแต่เริ่ม (ไม่รอ dots)
        renderRoundRobin(
                new BlockingQueue[]{ q1, q2, q3 },
                new Turtle[]{ bob1, bob2, bob3 }
        );

        tDots.join();
        tGreen.join();
        tBox.join();
    }
}
