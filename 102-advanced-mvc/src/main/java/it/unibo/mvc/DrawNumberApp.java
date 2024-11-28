package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final String configFile, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        final Configuration.Builder confBuilder = new Configuration.Builder();
        try (var br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(configFile)))){
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                final StringTokenizer st = new StringTokenizer(line, ":");
                switch (st.nextToken()) {
                    case "minimum":
                        confBuilder.setMin(Integer.parseInt(st.nextToken().trim()));
                        break;
                    case "maximum":
                        confBuilder.setMax(Integer.parseInt(st.nextToken().trim()));
                        break;
                    case "attempts":
                        confBuilder.setAttempts(Integer.parseInt(st.nextToken().trim()));
                        break;
                    default:
                        throw new IllegalStateException("I can't understand [" + line + "]");
                }
            }
        } catch (final Exception e) {
            for (final DrawNumberView view: views) {
                view.displayError(e.getMessage());
            }
        }
        Configuration configuration = confBuilder.build();
        this.model = new DrawNumberImpl(configuration);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp("config.yml",
                new DrawNumberViewImpl(),
                new DrawNumberViewImpl(),
                new PrintStreamView(System.out),
                new PrintStreamView("output.txt"));
    }

}
