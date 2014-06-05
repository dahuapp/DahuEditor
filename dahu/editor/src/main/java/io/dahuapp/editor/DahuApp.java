package io.dahuapp.editor;

import io.dahuapp.editor.kernel.module.Mouse;
import io.dahuapp.common.javascript.WebEngineRuntime;
import io.dahuapp.common.net.DahuFileAccessManager;
import io.dahuapp.common.net.DahuURLStreamHandlerFactory;
import io.dahuapp.editor.helper.OSCheck;
import io.dahuapp.editor.kernel.DahuAppKernel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.*;

/**
 * Main class of the application. Runs the GUI to allow the user to take
 * screenshots and edit the presentation he wants to make.
 */
public class DahuApp extends Application {

    /**
     * Title of the application.
     */
    public static final String TITLE = "DahuApp Editor";
    /**
     * Minimum width of the editor window.
     */
    private static final int MIN_WIDTH = 720;
    /**
     * Minimum height of the editor window.
     */
    private static final int MIN_HEIGHT = 520;

    /**
     * Application components
     */
    private WebView webView;
    private WebEngineRuntime webEngineRuntime;
    private DahuFileAccessManager dahuFileAccessManager;
    private MenuBar menuBar;

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* fix for osx */
        System.setProperty("javafx.macosx.embedded", "true");
        java.awt.Toolkit.getDefaultToolkit();

        /* launch app */
        launch(args);
    }

    /**
     * Gets the path of a ressource of the application.
     *
     * @param name Name of the resource to find.
     * @return The path to the resource (as it can be put in a file and opened).
     */
    public static String getResource(String name) {
        return DahuApp.class.getResource(name).toExternalForm();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // init dahu application
        initDahuApp(primaryStage);

        // init dahu menu
        initDahuMenu();

        // init layout
        initDahuLayout(primaryStage);

        // ready
        primaryStage.setTitle(TITLE);
        primaryStage.show();
    }

    /**
     * Initializes the WebView with the HTML content and binds the drivers to
     * the Dahuapp JavaScript object.
     *
     * @param primaryStage Main stage of the app (for the kernel).
     */
    private void initDahuApp(final Stage primaryStage) {
        webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.setFontSmoothingType(FontSmoothingType.LCD);

        // init JavascriptRuntime
        webEngineRuntime = new WebEngineRuntime(webView.getEngine());

        // init dahu file acess manager
        dahuFileAccessManager = new DahuFileAccessManager();

        // set custom URL stream factory
        URL.setURLStreamHandlerFactory(new DahuURLStreamHandlerFactory(
                getClass().getClassLoader(),
                dahuFileAccessManager));

        webView.getEngine().load("classpath:///io/dahuapp/core/app.html");

        // extend the webView js context
        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(final ObservableValue<? extends Worker.State> observableValue, final Worker.State oldState, final Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {

                    // load FirebugLite for debugging
                    webEngineRuntime.loadFirebugLite();

                    // load kernel
                    JSObject window = webEngineRuntime.getJSObject("window");
                    window.setMember("kernel", new DahuAppKernel(
                            primaryStage,
                            webEngineRuntime,
                            dahuFileAccessManager));

                    // start application
                    webEngineRuntime.executeScript("dahuapp.start();");
                }
            }
        });

        //@todo clean that
//        // adds a dialog alert handler
//        webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
//            @Override
//            public void handle(WebEvent<String> e) {
//                Dialogs.showMessage(e.getData());
//                e.consume();
//            }
//        });
//
//        // adds a confirm dialog handler
//        webView.getEngine().setConfirmHandler(new Callback<String, Boolean>() {
//            @Override
//            public Boolean call(String p) {
//                return Dialogs.showConfirm(p);
//            }
//        });
//
//        // adds a prompt dialog handler
//        webView.getEngine().setPromptHandler(new Callback<PromptData, String>() {
//            @Override
//            public String call(PromptData p) {
//                return Dialogs.showPrompt(p.getMessage(), p.getDefaultValue());
//            }
//        });
    }

    /**
     * Initialize Dahu main menu.
     * <p/>
     * We use native menu bar so that it can be better
     * handled by the OS (e.g. moved at the top in iOS)
     */
    private void initDahuMenu() {
        // File
        Menu menuFile = new Menu("File");

        // File > Open
        MenuItem menuFileOpen = new MenuItem("Open");
        menuFileOpen.setOnAction((event) -> {
            webEngineRuntime.executeScript("dahuapp.events.trigger('app:onFileOpen');");
        });

        menuFile.getItems().addAll(menuFileOpen);

        // Help
        Menu menuHelp = new Menu("Help");

        // Help > Tip of the Day
        MenuItem menuHelpTips = new MenuItem("Tip of the Day");
        menuHelpTips.setOnAction((event) -> {
            webEngineRuntime.executeScript("dahuapp.events.trigger('dahuapp:onHelpTips');");
        });

        // Help > Submit Feedback
        MenuItem menuHelpFeedback = new MenuItem("Submit Feedback");
        menuHelpFeedback.setOnAction((event) -> {
            webEngineRuntime.executeScript("dahuapp.events.trigger('dahuapp:onHelpFeedback');");
        });

        menuHelp.getItems().addAll(menuHelpTips, new SeparatorMenuItem(), menuHelpFeedback);

        // Create the main menu bar
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuFile, menuHelp);
    }

    private void initDahuLayout(Stage primaryStage) {
        StackPane rootPane = new StackPane();
        BorderPane borderPane = new BorderPane();

        OSCheck.OSType OS = OSCheck.getOperatingSystemType();

        // pin the menu
        borderPane.setTop(menuBar);
        if (OS == OSCheck.OSType.MacOS) {
            menuBar.setUseSystemMenuBar(true);
        }

        // pin the webView
        borderPane.setCenter(webView);

        // done
        rootPane.getChildren().add(borderPane);

        // create the scene
        Scene scene = new Scene(rootPane, MIN_WIDTH, MIN_HEIGHT);
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setOnCloseRequest((event) -> {
            webEngineRuntime.executeScript("dahuapp.stop();");
        });
    }
}
