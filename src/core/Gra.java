package core;

import javafx.beans.property.SimpleStringProperty;

public class Gra {
    private SimpleStringProperty nazwa, promocja;
    private boolean dostepnosc;
    private double cena;

    public Gra(String nazwa, String promocja, boolean dostepnosc, double cena) {
        this.nazwa = new SimpleStringProperty(nazwa);
        this.promocja = new SimpleStringProperty(promocja);
        this.dostepnosc = dostepnosc;
        this.cena = cena;
    }

    public String getNazwa() {
        return nazwa.get();
    }

    public SimpleStringProperty nazwaProperty() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa.set(nazwa);
    }

    public String getPromocja() {
        return promocja.get();
    }

    public SimpleStringProperty promocjaProperty() {
        return promocja;
    }

    public void setPromocja(String promocja) {
        this.promocja.set(promocja);
    }

    public boolean isDostepnosc() {
        return dostepnosc;
    }

    public void setDostepnosc(boolean dostepnosc) {
        this.dostepnosc = dostepnosc;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    @Override
    public String toString() {
        return "Gra{" +
                "nazwa=" + nazwa +
                ", promocja=" + promocja +
                ", dostepnosc=" + dostepnosc +
                ", cena=" + cena +
                '}';
    }
}