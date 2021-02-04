package core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Controller implements Initializable {
    private static List<Gra> listaGier = null;
    @FXML
    private TableView<Gra> tableView;
    @FXML
    private TableColumn<Gra, String> nameColumn;
    @FXML
    private TableColumn<Gra, String> discountColumn;
    @FXML
    private TableColumn<Gra, Boolean> availabilityColumn;
    @FXML
    private TableColumn<Gra, Double> priceColumn;

    @FXML
    private TextField searchField;

    @FXML
    private TextField cenaOd;

    @FXML
    private TextField cenaDo;

    @FXML
    private CheckBox tylkoDostepne;

    @FXML
    private CheckBox tylkoPromocje;

    @FXML
    private TextArea outputArea;

    @FXML
    private void btnStartScrap(ActionEvent event) {
        outputArea.appendText("Rozpoczynam proces pozyskiwania danych\n");
        ObservableList<Gra> gry = FXCollections.observableArrayList(getGryList(getDataFromPage(searchField.getText())));
        tableView.setItems(gry);
    }

    @FXML
    private void btnFiltr(ActionEvent event) {
        int cenaOdInt;
        int cenaDoInt;
        try {
            cenaOdInt = Integer.parseInt(cenaOd.getText());
        } catch (NumberFormatException e) {
            cenaOdInt = 0;
        }
        try {
            cenaDoInt = Integer.parseInt(cenaDo.getText());
        } catch (NumberFormatException e) {
            cenaDoInt = 0;
        }

        ObservableList<Gra> gry = FXCollections.observableArrayList(ustawieniaFiltrow(cenaOdInt, cenaDoInt, tylkoDostepne.isSelected(), tylkoPromocje.isSelected()));

//        tableView.getItems().clear();
        tableView.setItems(gry);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        outputArea.appendText("Wyszukiwarka gier ze strony Rebel.com\n\n");
        outputArea.appendText("     Opracował \n " +
                              "           --- Damian \n" +
                              "                     THE WALL \n " +
                              "                              Świerszcz ---\n\n");
        outputArea.appendText("Aby pobrać dane nalezy wprowadzić\n nazwę przedmiotu jakiego szukamy\n");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("cena"));
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("dostepnosc"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("promocja"));
    }

    private List<String> getDataFromPage(String search) {
        outputArea.appendText("Wyszukuję przedmiotu: [" + search +"]\n . . . . \n");
        System.setProperty("webdriver.chrome.driver", "resources\\chromedriver.exe");
        if (System.getProperty("webdriver.chrome.driver").isEmpty() || System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver","C:\\rebelScrapper\\chromedriver.exe");
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080", "--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get("https://www.rebel.pl/");

        //Wyszukiwanie frazy
        WebElement wyszukiwarka = driver.findElement(By.name("phrase"));
        wyszukiwarka.sendKeys(search + Keys.ENTER);

        //Wynik wyszukiwania
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        String wynikString = driver.findElement(By.className("c-filters__results-value")).getText();
        try {
            int wyniki = Integer.parseInt(wynikString.substring(0, wynikString.indexOf(' ')));
            outputArea.appendText("Znaleziono: [" + wyniki +"] pozycji\n");
        } catch (NumberFormatException e) {
            outputArea.appendText("Coś pozło nie tak - spróbuj jeszcze raz\n");
        }

        List<WebElement> listaWynikow;
        List<String> przedmioty = new ArrayList<>();
        String source;
        boolean kolejnaStrona = true;
        while (kolejnaStrona) {
            listaWynikow = driver.findElements(By.xpath("//div[@class='col-6 col-md-4 col-xl-3 col-hd-2']"));
            for (WebElement webElement : listaWynikow) {
                source = webElement.getAttribute("innerHTML");
                przedmioty.add(source);
            }
            try {
                driver.findElement(By.cssSelector("a[class='page-link next']")).click();
                TimeUnit.SECONDS.sleep(2);
            } catch (NoSuchElementException e) {
                kolejnaStrona = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        driver.quit();

        return przedmioty;
    }

    private ObservableList<Gra> getGryList(List<String> przedmioty) {
        outputArea.appendText("Przetwarzam listę\n");
        ObservableList<Gra> gry = FXCollections.observableArrayList();
        String nazwa, promocja = null;
        boolean dostepnosc;
        double cena;
        Document document;
        for (String html : przedmioty) {
            document = Jsoup.parse(html);
            nazwa = document.select("h3").first().text();
            dostepnosc = document.selectFirst("div.product__short-desc.product__short-desc--center > div.product__delivery.text-center").text().contains("niedost");
            String cenaRaw = document.selectFirst("span.price.product__price").text();
            cena = Double.parseDouble(
                    (cenaRaw.substring(0, cenaRaw.length() - 5)) + "." + cenaRaw.substring(cenaRaw.length() - 5, cenaRaw.length() - 3));
            try {
                promocja = document.selectFirst("div[class='product__label product__label--discount']").text();
            } catch (Exception e) {
                promocja = "";
            }

            gry.add(new Gra(nazwa, promocja, dostepnosc, cena));
        }
        listaGier = gry;
        return gry;
    }

    private ObservableList<Gra> ustawieniaFiltrow(int cenaOd, int cenaDo, boolean tylkoDostepne, boolean tylkoPromocje) {
        outputArea.appendText("Przetwarzam liste o filtry jakie zastały zastosowane\n");
        List<Gra> lista = new ArrayList<>(listaGier);
        if (cenaOd > 0) {
            Predicate<Gra> filtr = list -> list.getCena() < cenaOd;
            lista.removeIf(filtr);
        }
        if (cenaDo > 0 && cenaDo > cenaOd) {
            Predicate<Gra> filtr = list -> list.getCena() > cenaDo;
            lista.removeIf(filtr);
        }
        if (tylkoDostepne) {
            Predicate<Gra> filtr = list -> list.isDostepnosc() != tylkoDostepne;
            lista.removeIf(filtr);
        }
        if (tylkoPromocje) {
            Predicate<Gra> filtr = list -> list.getPromocja().isEmpty();
            lista.removeIf(filtr);
        }
        return FXCollections.observableArrayList(lista);
    }
}